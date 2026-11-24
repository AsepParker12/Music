package com.music.app.feature.playback

import android.content.Context
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.kyant.taglib.TagLib
import com.music.app.core.common.format.repairedTagText
import com.music.app.core.data.LibraryRepository
import com.music.app.core.data.contentUri
import com.music.app.core.data.lyrics.LyricsEntry
import com.music.app.core.data.lyrics.LyricsUtils
import com.music.app.core.datastore.PlaybackSettings
import com.music.app.core.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How often the line on the lock screen is looked at again while a track plays. */
private const val LineCheckMs = 500L

/** Lead time offset for LRC-style line-synced lyrics (ms) - the player's own, in `LyricsTimeline`. */
private const val LrcLeadMs = 300L

/** Lead time offset for TTML word-synced lyrics (ms) - the player's own, in `LyricsTimeline`. */
private const val TtmlLeadMs = 0L

/** How far ahead of its time a line is shown, so it is there as it is sung - the player's own tuning. */
private const val VisualTuningMs = 150L

/**
 * How many times a track found without synced lyrics has its file read again while it plays. The one writer
 * that fills a file in unasked - the missing-lyrics finder - waits on every one of its sources before it
 * writes, which takes well past a few seconds; the reads run on for about two minutes before giving the
 * track up for this play of it.
 */
private const val FileReadAttempts = 12

/** The wait between those reads. */
private const val FileReadRetryMs = 10_000L

/**
 * The playing track's synced lyrics on the lock screen and in the notification, while the user has them on:
 * the line playing stands where the album stands in the playing item's metadata, and where the artist stands
 * too. The artist's place is the one that decides it on the phone this is built for: its lock screen card
 * shows two lines of text alone - the title and the artist - and no album at all, so a line standing only in
 * the album's place would never be seen there; a surface that does show the album has the line there as well.
 * The title is never the line's to take. The track's own artist and album stand there again wherever there is
 * no line to show: the setting off, no synced lyrics in the track's file, a wordless stretch of the song.
 *
 * The lyrics are the file's own first, the very ones the player shows: the tag editor's repository that
 * reads them for the player cannot be reached from here - it is built on this module - so the one tag is
 * read from the file directly, by the same TagLib call the editor makes. A file holding none that are
 * synced - the common case for a downloaded song, whose lyrics nothing has written in: the finder that
 * would is let write files only where Android lets Music change them unasked - is not the end of it:
 * LRCLIB is searched from here, as the editor searches it, and what it gives is held in memory alone -
 * see [LockScreenLyricsFetch]. The parsing either way is [LyricsUtils], the player's own, with its own
 * lead times, so the line here changes as the line in the player does.
 *
 * A line is put on the item by replacing the item with itself, only its artist and album text changed: the
 * file it opens is the same, so nothing that plays is interrupted - the in-place replacement
 * [PlaybackService.followLibrary] already keeps the queue's metadata current by. The item is replaced only
 * where the text it carries is not already the text wanted, and that text changes only as the line does.
 *
 * The player's own lyrics can be shifted by hand while they are open; that shift is the player screen's
 * alone, held in its own state where no service can read it, so the line here follows the file's timing
 * as written.
 */
internal class LockScreenLyrics(
    private val context: Context,
    private val player: Player,
    private val settings: PlaybackSettings,
    private val library: LibraryRepository,
    private val scope: CoroutineScope,
) : Player.Listener {

    private val ticker = Handler(player.applicationLooper)
    private val tick = object : Runnable {
        override fun run() {
            refreshLine()
            ticker.postDelayed(this, LineCheckMs)
        }
    }

    @Volatile private var isEnabled = false

    // The track the loaded entries belong to, and the entries themselves. Touched on the main thread alone -
    // the player's callbacks, the ticker, and the settings collector all run there - apart from the file
    // read, which is handed over through the scope and lands back on it.
    private var loadedTrackId: Long? = null
    private var entries: List<LyricsEntry> = emptyList()
    private var leadMs: Long = LrcLeadMs

    /** The load running for the loaded track - the file reads and the web search together - let go with it. */
    private var loadJob: Job? = null

    /** Whether the loaded track's synced lyrics are in hand, from its file or from the web. */
    private var lyricsReady = false

    fun attach() {
        player.addListener(this)
        scope.launch {
            settings.lockScreenLyrics.flow.collect { enabled ->
                isEnabled = enabled
                // A setting turned under a playing track takes effect at once, either way.
                loadLyricsForCurrentTrack()
                refreshLine()
                updateTicker()
            }
        }
    }

    fun detach() {
        ticker.removeCallbacks(tick)
        loadJob?.cancel()
        player.removeListener(this)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        // A new item carries its own album: whatever line the last one showed is gone with it.
        loadJob?.cancel()
        loadedTrackId = null
        entries = emptyList()
        lyricsReady = false
        loadLyricsForCurrentTrack()
        refreshLine()
        updateTicker()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) = updateTicker()

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        // A seek lands on another line at once, rather than at the next check.
        if (oldPosition.mediaItemIndex == newPosition.mediaItemIndex) refreshLine()
    }

    private fun updateTicker() {
        ticker.removeCallbacks(tick)
        if (isEnabled && player.isPlaying) ticker.postDelayed(tick, LineCheckMs)
    }

    private fun currentTrack(): Track? {
        val trackId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return null
        return library.tracksById.value[trackId]
    }

    /**
     * Loads the current track's synced lyrics, from wherever they come first: its file, read as the player
     * reads it and read again while the track plays - the missing-lyrics finder may write them into it
     * meanwhile, late, as it waits on all of its sources before writing - and LRCLIB, searched alongside
     * for a file that holds none. Whichever gives synced lyrics first is the one that stands; a track given
     * up on is looked for again the next time it comes round.
     */
    private fun loadLyricsForCurrentTrack() {
        if (!isEnabled) {
            loadJob?.cancel()
            return
        }
        val track = currentTrack() ?: return
        if (loadedTrackId == track.id) return
        loadedTrackId = track.id
        lyricsReady = false
        loadJob?.cancel()
        loadJob = scope.launch {
            launch { readFileLyrics(track) }
            launch {
                val fetched = LockScreenLyricsFetch.syncedLyrics(track) ?: return@launch
                if (loadedTrackId != track.id || lyricsReady) return@launch
                applyParsed(track, parse(fetched, track.durationMs))
            }
        }
    }

    /** [track]'s file, read for its lyrics and read again while it plays - see [loadLyricsForCurrentTrack]. */
    private suspend fun readFileLyrics(track: Track) {
        var attempt = 0
        while (true) {
            val raw = withContext(Dispatchers.IO) { readLyricsText(track) }
            if (loadedTrackId != track.id || lyricsReady) return
            val parsed = parse(raw, track.durationMs)
            if (parsed.first.isNotEmpty()) {
                applyParsed(track, parsed)
                return
            }
            if (attempt >= FileReadAttempts - 1) return
            attempt++
            delay(FileReadRetryMs)
            if (loadedTrackId != track.id || lyricsReady) return
        }
    }

    /** Stands [parsed] as the loaded track's lyrics, where it is still that track and none stand yet. */
    private fun applyParsed(track: Track, parsed: Pair<List<LyricsEntry>, Long>) {
        if (parsed.first.isEmpty() || loadedTrackId != track.id || lyricsReady) return
        entries = parsed.first
        leadMs = parsed.second
        lyricsReady = true
        refreshLine()
    }

    /**
     * [raw] lyrics as the player's timeline has them: their entries - none where they are not synced - and
     * the lead their kind is shown with. A wordless entry stands at the head, as in the player, so the stretch
     * before the first line shows the album rather than a line sung early.
     */
    private fun parse(raw: String, durationMs: Long): Pair<List<LyricsEntry>, Long> {
        if (raw.isBlank()) return emptyList<LyricsEntry>() to LrcLeadMs
        val isWordSynced = LyricsUtils.isTtml(raw)
        val isSynced = isWordSynced || LyricsUtils.isLineSyncedLrc(raw)
        if (!isSynced) return emptyList<LyricsEntry>() to LrcLeadMs
        val parsed = if (isWordSynced) {
            LyricsUtils.parseTtml(raw)
        } else {
            LyricsUtils.insertInstrumentalBreaks(LyricsUtils.parseLyrics(raw), durationMs.coerceAtLeast(0L))
        }
        val entries = if (parsed.isNotEmpty() && parsed.first().time >= 0) {
            listOf(LyricsEntry(time = 0L, text = "")) + parsed
        } else {
            parsed
        }
        return entries to if (isWordSynced) TtmlLeadMs else LrcLeadMs
    }

    /**
     * The lyrics [track]'s file holds, as written - blank where it holds none. The editor's own read, in
     * `TrackTagsRepository`, takes the same TagLib property of the same name; this is that read without the
     * editor around it.
     */
    private fun readLyricsText(track: Track): String = runCatching {
        val descriptor = context.contentResolver.openFileDescriptor(track.contentUri, "r") ?: return ""
        descriptor.use { file ->
            // TagLib takes the descriptor it is handed as its own and closes it: it is given a duplicate.
            val properties = TagLib.getMetadata(file.dup().detachFd(), readPictures = false)?.propertyMap
            properties?.get("LYRICS")?.firstOrNull()?.repairedTagText()?.trim().orEmpty()
        }
    }.getOrDefault("")

    /**
     * Stands the playing line where the current item's artist and album stand, or the track's own artist and
     * album back - only where what stands there is not already that, so the item is replaced only as the line
     * changes. The artist carries the line because the lock screen card shows the artist under the title and
     * no album; the album carries it too, for the surfaces that show an album instead.
     */
    private fun refreshLine() {
        val item = player.currentMediaItem ?: return
        val track = currentTrack() ?: return
        val line = when {
            !isEnabled -> null
            loadedTrackId != track.id -> null
            else -> lineAt(player.currentPosition)
        }
        val desiredAlbum = line ?: track.album
        val desiredArtist = line ?: track.artist
        if (item.mediaMetadata.albumTitle?.toString() == desiredAlbum &&
            item.mediaMetadata.artist?.toString() == desiredArtist
        ) {
            return
        }
        val metadata = item.mediaMetadata.buildUpon()
            .setAlbumTitle(desiredAlbum)
            .setArtist(desiredArtist)
            .build()
        player.replaceMediaItem(player.currentMediaItemIndex, item.buildUpon().setMediaMetadata(metadata).build())
    }

    /** The line playing at [positionMs], or null where that stretch of the song has none to show. */
    private fun lineAt(positionMs: Long): String? {
        if (entries.isEmpty()) return null
        val index = LyricsUtils.findCurrentLineIndex(entries, positionMs + leadMs + VisualTuningMs, 0L)
        return entries.getOrNull(index)?.text?.takeIf { it.isNotBlank() }
    }
}
