package com.music.app.feature.library.quickplay

import androidx.compose.ui.graphics.vector.ImageVector
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.model.PlaybackParent

/** The kinds of collection quick play can be set to play, each chosen on its own picker. */
enum class QuickPlaySourceKind(internal val icon: ImageVector) {
    PLAYLIST(MusicIcons.Playlist),
    ARTIST(MusicIcons.Artist),
    ALBUM(MusicIcons.Album),
    GENRE(MusicIcons.Genre),
    FOLDER(MusicIcons.Folder),
}

/**
 * The kind of collection [this] is, or null for what is chosen without a picker - the listening histories,
 * which are one each - and for the videos, never a quick play source.
 */
val PlaybackParent.quickPlaySourceKind: QuickPlaySourceKind?
    get() = when (this) {
        is PlaybackParent.Playlist -> QuickPlaySourceKind.PLAYLIST
        is PlaybackParent.Artist -> QuickPlaySourceKind.ARTIST
        is PlaybackParent.Album -> QuickPlaySourceKind.ALBUM
        is PlaybackParent.Genre -> QuickPlaySourceKind.GENRE
        is PlaybackParent.Folder -> QuickPlaySourceKind.FOLDER
        PlaybackParent.Videos, PlaybackParent.RecentlyPlayed, PlaybackParent.MostPlayed -> null
    }
