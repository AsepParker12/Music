package com.music.app.feature.library.options

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.feature.library.R

/**
 * Every row an options sheet can offer, icon and label together - Auxio's `action_*` menu items.
 *
 * [VIEW_DETAILS] is a song's own "View properties" as much as an album's, artist's, genre's or
 * playlist's "View" - two labels over the one glyph, the way Auxio's `lbl_song_detail` and
 * `lbl_parent_detail` are. [ADD_TRACKS], [ADD_COLLECTIONS], [EXCLUDE] and [REMOVE_FROM_PLAYLIST] are
 * Music's own, with no Auxio glyph to draw, so they draw the Material glyphs the playlist's menu and the
 * selection bar already use.
 *
 * [DELETE] deletes a playlist, and never a file; [DELETE_FROM_DEVICE] deletes tracks' files - Fossify's
 * delete - so the two are told apart by name wherever both could be read.
 * Which [OptionsAction]s a sheet shows, and in what order, is [OptionsTarget]'s
 * to decide - this only says what each one looks like once chosen.
 */
enum class OptionsAction(val icon: ImageVector, @StringRes val labelRes: Int) {
    PLAY(MusicIcons.Play, R.string.options_play),
    SHUFFLE(MusicIcons.Shuffle, R.string.options_shuffle),
    PLAY_NEXT(MusicIcons.PlayNext, R.string.options_play_next),
    QUEUE_ADD(MusicIcons.QueueAdd, R.string.options_queue_add),
    PLAYLIST_ADD(MusicIcons.PlaylistAdd, R.string.options_playlist_add),
    ARTIST_DETAILS(MusicIcons.GoToArtist, R.string.options_artist_details),
    ALBUM_DETAILS(MusicIcons.GoToAlbum, R.string.options_album_details),
    SONG_PROPERTIES(MusicIcons.Details, R.string.options_song_properties),
    EDIT_TAGS(MusicIcons.EditNote, R.string.options_edit_tags),
    AUTO_TAG(MusicIcons.AutoFixHigh, R.string.options_auto_tag),
    STOP_COUNTING_PLAYS(MusicIcons.TrendingFlat, R.string.options_stop_counting_plays),
    COUNT_PLAYS(MusicIcons.TrendingUp, R.string.options_count_plays),
    VIEW_DETAILS(MusicIcons.Details, R.string.options_view_details),
    BROWSE_FOLDER(MusicIcons.FolderOpen, R.string.options_browse_folder),
    ADD_TRACKS(MusicIcons.PlaylistAdd, R.string.options_add_tracks),
    ADD_COLLECTIONS(MusicIcons.LibraryAdd, R.string.options_add_collections),
    EDIT(MusicIcons.Edit, R.string.options_edit),
    IMPORT(MusicIcons.Import, R.string.options_import),
    EXPORT(MusicIcons.Export, R.string.options_export),
    DELETE(MusicIcons.Delete, R.string.options_delete),
    REMOVE_FROM_PLAYLIST(MusicIcons.RemoveCircleOutline, R.string.options_remove_from_playlist),
    REMOVE_FROM_RECENTLY_PLAYED(MusicIcons.RemoveCircleOutline, R.string.options_remove_from_recently_played),
    REMOVE_FROM_MOST_PLAYED(MusicIcons.RemoveCircleOutline, R.string.options_remove_from_most_played),
    ADD_TO_PRIVATE(MusicIcons.Lock, R.string.options_add_to_private),
    DELETE_FROM_DEVICE(MusicIcons.Delete, R.string.options_delete_from_device),
    EXCLUDE(MusicIcons.Block, R.string.options_exclude),
    SHARE(MusicIcons.Share, R.string.options_share),
}

/** What [this] action is called, as every sheet, menu and bar names it. */
val OptionsAction.label: String
    @Composable get() = stringResource(labelRes)
