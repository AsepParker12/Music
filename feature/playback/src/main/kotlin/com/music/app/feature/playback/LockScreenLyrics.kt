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

/** How many times a track found without lyrics is read again while it plays, in case they are being fetched. */
private const val LyricsReadAttempts = 3

/** The wait between those reads. */
private const val LyricsReadRetryMs = 10_000L

/**
 * The playing track's synced lyrics on the lock screen and in the notification, while the user has them on:
 * the line playing stands where the album stands in the playing item's metadata - the one text those surfaces
 * show under the title that a line can take - and the album stands there again wherever there is no line to
 * show: the setting off, no synced lyrics in the track's file, a wordless stretch of the song.
 *
 * The lyrics are the file's own, the very ones the player shows: the tag editor's repository that reads them
 * for the player cannot be reached from here - it is built on this module - so the one tag is read from the
 * file directly, by the same TagLib call the editor makes. The parsing is [LyricsUtils], the player's own,
 * with its own lead times, so the line here changes as the line in the player does.
 *
 * A line is put on the item by replacing the item with itself, only its album text changed: the file it
 * opens is the same, so nothing that plays is interrupted - the in-place replacement
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
        player.removeListener(this)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        // A new item carries its own album: whatever line the last one showed is gone with it.
        loadedTrackId = null
        entries = emptyList()
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
     * Reads the current track's file for its lyrics and parses them as the player does. Read again while the
     * track plays when the file holds none yet, as they may be being looked for on the web meanwhile and
     * written into the file when found - the player's own way of coming by them.
     */
    private fun loadLyricsForCurrentTrack() {
        if (!isEnabled) return
        val track = currentTrack() ?: return
        if (loadedTrackId == track.id) return
        loadedTrackId = track.id
        scope.launch {
            var attempt = 0
            while (true) {
                val raw = withContext(Dispatchers.IO) { readLyricsText(track) }
                if (loadedTrackId != track.id) return@launch
                val parsed = parse(raw, track.durationMs)
                entries = parsed.first
                leadMs = parsed.second
                refreshLine()
                if (entries.isNotEmpty() || attempt >= LyricsReadAttempts - 1) break
                attempt++
                delay(LyricsReadRetryMs)
                if (loadedTrackId != track.id) return@launch
            }
        }
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
     * Stands the playing line where the current item's album stands, or the album back - only where what
     * stands there is not already that, so the item is replaced only as the line changes.
     */
    private fun refreshLine() {
        val item = player.currentMediaItem ?: return
        val track = currentTrack()
        val desired = when {
            track == null -> return
            !isEnabled -> track.album
            loadedTrackId != track.id -> track.album
            else -> lineAt(player.currentPosition) ?: track.album
        }
        if (item.mediaMetadata.albumTitle?.toString() == desired) return
        val metadata = item.mediaMetadata.buildUpon().setAlbumTitle(desired).build()
        player.replaceMediaItem(player.currentMediaItemIndex, item.buildUpon().setMediaMetadata(metadata).build())
    }

    /** The line playing at [positionMs], or null where that stretch of the song has none to show. */
    private fun lineAt(positionMs: Long): String? {
        if (entries.isEmpty()) return null
        val index = LyricsUtils.findCurrentLineIndex(entries, positionMs + leadMs + VisualTuningMs, 0L)
        return entries.getOrNull(index)?.text?.takeIf { it.isNotBlank() }
    }
}
