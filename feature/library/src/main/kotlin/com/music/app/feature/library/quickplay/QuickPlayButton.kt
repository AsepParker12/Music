package com.music.app.feature.library.quickplay

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.music.app.core.data.quickplay.QuickPlay
import com.music.app.core.data.quickplay.QuickPlaySource
import com.music.app.core.datastore.QuickPlayMode
import com.music.app.core.designsystem.component.fab.FloatingActionButtonMenuChoice
import com.music.app.core.designsystem.component.fab.FloatingActionButtonMenuContent
import com.music.app.core.designsystem.component.fab.MusicFloatingActionButtonMenu
import com.music.app.core.designsystem.component.icon
import com.music.app.core.designsystem.component.titleRes
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.model.BuiltInList
import com.music.app.core.model.PlaybackParent
import com.music.app.core.model.builtInList
import com.music.app.feature.library.R

/**
 * The library's quick play button - Auxio's home shuffle FAB, which plays [quickPlay]'s source as its mode
 * says: shuffled or in order, its icon saying which. A tap plays exactly what it shows. A long press opens its
 * menu to play another: every track, Favorites, the two listening histories, the collection chosen in the
 * settings while that is the source, or Other, which opens the settings to choose any collection. The source
 * is marked.
 *
 * Picking one makes it the source and plays it at once, so what the button plays from then on is always what
 * it last played. It stands at the bottom of the screen's FAB stack - see [MusicFloatingActionButtonMenu] - and
 * the stack reads [quickPlay] and [visible] where it draws it, so both read state: a change to either is drawn
 * in the frame it happens in. [quickPlay] is null while the user has turned the button off.
 */
@Composable
internal fun QuickPlayButton(
    quickPlay: () -> QuickPlay?,
    visible: () -> Boolean,
    favoritesPlaylistId: Long,
    onPlay: (QuickPlay) -> Unit,
    onPlayFrom: (PlaybackParent?, QuickPlayMode) -> Unit,
    onChooseOther: () -> Unit,
) {
    val favorites = PlaybackParent.Playlist(favoritesPlaylistId)
    // Read through the context: the menu is built outside composition.
    val context = LocalContext.current
    MusicFloatingActionButtonMenu {
        val current = quickPlay() ?: return@MusicFloatingActionButtonMenu null
        val source = current.source
        val playFrom: (PlaybackParent?) -> Unit = { parent -> onPlayFrom(parent, current.mode) }
        FloatingActionButtonMenuContent(
            icon = when (current.mode) {
                QuickPlayMode.SHUFFLE -> MusicIcons.Shuffle
                QuickPlayMode.PLAY -> MusicIcons.Play
            },
            contentDescription = when (current.mode) {
                QuickPlayMode.SHUFFLE -> if (source == QuickPlaySource.AllTracks) {
                    context.getString(R.string.quick_play_shuffle_all)
                } else {
                    context.getString(R.string.quick_play_shuffle, source.label(context))
                }
                QuickPlayMode.PLAY -> if (source == QuickPlaySource.AllTracks) {
                    context.getString(R.string.quick_play_play_all)
                } else {
                    context.getString(R.string.quick_play_play, source.label(context))
                }
            },
            visible = visible(),
            onClick = { onPlay(current) },
            choices = listOfNotNull(
                FloatingActionButtonMenuChoice(
                    label = QuickPlaySource.AllTracks.label(context),
                    icon = MusicIcons.Song,
                    isSelected = source == QuickPlaySource.AllTracks,
                    onClick = { playFrom(null) },
                ),
                FloatingActionButtonMenuChoice(
                    label = context.getString(BuiltInList.FAVORITES.titleRes),
                    icon = BuiltInList.FAVORITES.icon,
                    isSelected = source.parent == favorites,
                    onClick = { playFrom(favorites) },
                ),
                FloatingActionButtonMenuChoice(
                    label = QuickPlaySource.RecentlyPlayed.label(context),
                    icon = BuiltInList.RECENTLY_PLAYED.icon,
                    isSelected = source == QuickPlaySource.RecentlyPlayed,
                    onClick = { playFrom(PlaybackParent.RecentlyPlayed) },
                ),
                FloatingActionButtonMenuChoice(
                    label = QuickPlaySource.MostPlayed.label(context),
                    icon = BuiltInList.MOST_PLAYED.icon,
                    isSelected = source == QuickPlaySource.MostPlayed,
                    onClick = { playFrom(PlaybackParent.MostPlayed) },
                ),
                if (source is QuickPlaySource.Collection && source.parent != favorites) {
                    FloatingActionButtonMenuChoice(
                        label = source.name,
                        icon = source.parent.quickPlaySourceKind?.icon ?: MusicIcons.Playlist,
                        isSelected = true,
                        onClick = { playFrom(source.parent) },
                    )
                } else {
                    null
                },
                FloatingActionButtonMenuChoice(
                    label = context.getString(R.string.quick_play_other),
                    icon = MusicIcons.MoreVert,
                    onClick = onChooseOther,
                ),
            ),
        )
    }
}

private fun QuickPlaySource.label(context: Context): String = when (this) {
    QuickPlaySource.AllTracks -> context.getString(R.string.quick_play_all_tracks)
    QuickPlaySource.RecentlyPlayed -> context.getString(BuiltInList.RECENTLY_PLAYED.titleRes)
    QuickPlaySource.MostPlayed -> context.getString(BuiltInList.MOST_PLAYED.titleRes)
    is QuickPlaySource.Collection -> parent.builtInList?.let { context.getString(it.titleRes) } ?: name
}
