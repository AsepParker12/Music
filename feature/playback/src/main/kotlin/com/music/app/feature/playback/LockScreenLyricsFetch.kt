package com.music.app.feature.playback

import com.music.app.core.model.Track
import com.music.app.core.model.UnknownNames
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** How near a recording's length has to be to the track's for lyrics timed to it to fit - LRCLIB's own margin, as the tag editor's source has it. */
private const val MaxDurationDeltaSeconds = 2

private const val ConnectTimeoutMs = 10_000

private const val ReadTimeoutMs = 10_000

/**
 * Synced lyrics for the lock screen from LRCLIB - the same open library the tag editor's `LrcLibSource`
 * searches, and searched the same way: by the track's title, artist and album, its entries within two
 * seconds of the track's length, the nearest first, its synced lyrics the ones wanted.
 *
 * It is searched from here, slim, rather than the editor's source being shared: the editor is built on this
 * module - its repository writes files through this module's `TrackFiles`, and its finder follows this
 * module's playback - so nothing of it can be depended on from here without a cycle. What the two do share
 * is the parsing, `LyricsUtils` in core, so lyrics had this way are read exactly as the player's are.
 *
 * What is found is held in memory alone, never written into the file: writing a file is the editor's and the
 * missing-lyrics finder's work, with the care they take over files that are not laid out plainly. A track is
 * searched once a load, and one the finder fills in later is picked up from its file by the reads that run
 * alongside this search - see [LockScreenLyrics].
 */
internal object LockScreenLyricsFetch {

    /** [track]'s synced lyrics as LRCLIB publishes them - LRC text - or null where it has none that fit. */
    suspend fun syncedLyrics(track: Track): String? {
        if (track.isVideo || track.title.isBlank()) return null
        val url = buildString {
            append("https://lrclib.net/api/search?track_name=").append(encode(track.title))
            append("&artist_name=").append(encode(track.artist))
            track.album.takeIf { it.isNotBlank() && it != UnknownNames.ALBUM }
                ?.let { append("&album_name=").append(encode(it)) }
        }
        val body = withContext(Dispatchers.IO) { get(url) } ?: return null
        return runCatching { pick(body, track.durationMs) }.getOrNull()
    }

    /** The synced lyrics of the entry timed nearest a track [durationMs] long, among those within its margin - or null. */
    private fun pick(body: String, durationMs: Long): String? {
        val entries = JSONArray(body)
        val durationSeconds = (durationMs / 1000).toInt()
        var best: String? = null
        var bestDelta = Int.MAX_VALUE
        for (index in 0 until entries.length()) {
            val entry = entries.optJSONObject(index) ?: continue
            val synced = entry.optString("syncedLyrics")
            if (synced.isBlank()) continue
            if (durationSeconds <= 0) {
                // No length to fit against: the first with synced lyrics stands.
                if (best == null) best = synced
                continue
            }
            val entryDuration = entry.optDouble("duration", Double.NaN)
            if (entryDuration.isNaN()) continue
            val delta = abs(entryDuration.toInt() - durationSeconds)
            if (delta > MaxDurationDeltaSeconds || delta >= bestDelta) continue
            bestDelta = delta
            best = synced
        }
        return best
    }

    private fun get(url: String): String? = runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = ConnectTimeoutMs
            connection.readTimeout = ReadTimeoutMs
            connection.setRequestProperty("User-Agent", "Music (Android; lock screen lyrics)")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
