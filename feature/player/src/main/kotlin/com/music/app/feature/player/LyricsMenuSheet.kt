package com.music.app.feature.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.component.CoverArtDefaults
import com.music.app.core.designsystem.component.MusicCoverArt
import com.music.app.core.designsystem.component.sheet.MusicBottomSheet
import com.music.app.core.designsystem.component.sheet.MusicOptionRow
import com.music.app.core.designsystem.component.sheet.MusicOptionsSheetHeader
import com.music.app.core.model.Track
import com.music.app.feature.playback.R as PlaybackR
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.designsystem.component.MusicSwitch

/**
 * The open lyrics' overflow menu: first what the player puts away while its lyrics are open - the favourite,
 * the queue and the sleep timer - then ArchiveTune's `LyricsMenu` without the actions that go online - editing
 * the lyrics, nudging their timing, and showing or hiding the player controls beneath them - laid out as
 * every options sheet in the app is: the track's cover, what the menu is for, its name and artist, then its
 * options.
 *
 * Editing opens the lyrics editor - [onEditLyrics] - and is done with the menu. As with every options sheet, an
 * option that opens a dialog takes the sheet out of sight, and the menu is done once the dialog is; the switch
 * changes in place. The menu is about the track it opened for: should another start playing meanwhile, it
 * closes, rather than edit or retime lyrics that are no longer the ones on screen.
 */
@Composable
internal fun LyricsMenuSheet(
    track: Track,
    isFavorite: Boolean,
    /** Null for a track that cannot be a favourite, which leaves Favourite out. */
    onToggleFavorite: (() -> Unit)?,
    onOpenQueue: () -> Unit,
    onSleepTimerClick: () -> Unit,
    lyricsSyncOffset: Int,
    onLyricsSyncOffsetChange: (Int) -> Unit,
    showPlayerControls: Boolean,
    onShowPlayerControlsChange: (Boolean) -> Unit,
    /** Null for lyrics the editor cannot open - a Private Folder item's - which leaves Edit out. */
    onEditLyrics: (() -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val openedForTrackId = rememberSaveable { track.id }
    if (track.id != openedForTrackId) {
        LaunchedEffect(Unit) { onDismissRequest() }
        return
    }
    var isSettingSyncOffset by rememberSaveable { mutableStateOf(false) }

    if (!isSettingSyncOffset) {
        MusicBottomSheet(
            onDismissRequest = onDismissRequest,
            header = {
                MusicOptionsSheetHeader(
                    cover = { MusicCoverArt(coverArtUri = track.coverArtUri, contentDescription = null, size = CoverArtDefaults.OptionsHeaderSize) },
                    type = stringResource(R.string.player_lyrics),
                    name = track.title,
                    info = track.artist,
                )
            },
        ) {
            if (onToggleFavorite != null) {
                MusicOptionRow(
                    label = stringResource(
                        if (isFavorite) PlaybackR.string.playback_action_remove_favorite else PlaybackR.string.playback_action_add_favorite,
                    ),
                    icon = if (isFavorite) MusicIcons.Favorite else MusicIcons.FavoriteBorder,
                    onClick = {
                        onToggleFavorite()
                        onDismissRequest()
                    },
                )
            }
            MusicOptionRow(
                label = stringResource(R.string.player_queue),
                icon = MusicIcons.QueueMusic,
                onClick = {
                    onOpenQueue()
                    onDismissRequest()
                },
            )
            MusicOptionRow(
                label = stringResource(R.string.player_sleep_timer),
                icon = MusicIcons.Bedtime,
                onClick = {
                    onSleepTimerClick()
                    onDismissRequest()
                },
            )
            if (onEditLyrics != null) {
                MusicOptionRow(
                    label = stringResource(R.string.player_edit),
                    icon = MusicIcons.Edit,
                    onClick = {
                        onEditLyrics()
                        onDismissRequest()
                    },
                )
            }
            MusicOptionRow(
                label = stringResource(R.string.player_lyrics_sync_offset),
                icon = MusicIcons.Speed,
                detail = formatLyricsSyncOffset(lyricsSyncOffset),
                onClick = { isSettingSyncOffset = true },
            )
            MusicOptionRow(
                label = stringResource(R.string.player_show_lyrics_player_controls),
                icon = MusicIcons.PlayCircle,
                trailingContent = { MusicSwitch(checked = showPlayerControls, onCheckedChange = onShowPlayerControlsChange) },
                onClick = { onShowPlayerControlsChange(!showPlayerControls) },
            )
        }
    } else {
        LyricsSyncOffsetDialog(
            lyricsSyncOffset = lyricsSyncOffset,
            onDismiss = onDismissRequest,
            onConfirm = {
                onLyricsSyncOffsetChange(it)
                onDismissRequest()
            },
        )
    }
}
