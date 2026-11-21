package com.music.app.feature.library.uncounted

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.designsystem.component.LocalBottomContentPadding
import com.music.app.core.designsystem.component.MusicEmptyState
import com.music.app.core.designsystem.component.MusicTopAppBar
import com.music.app.core.designsystem.component.TopBarAction
import com.music.app.core.designsystem.component.fastscroll.FastScroller
import com.music.app.core.designsystem.component.rememberSelectionState
import com.music.app.core.designsystem.component.selectAllAction
import com.music.app.core.designsystem.component.toTopBarSelection
import com.music.app.core.model.Album
import com.music.app.core.model.Artist
import com.music.app.core.model.Folder
import com.music.app.core.model.Genre
import com.music.app.core.model.Playlist
import com.music.app.core.model.toStorageKey
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.library.AlbumRow
import com.music.app.feature.library.ArtistRow
import com.music.app.feature.library.FolderRow
import com.music.app.feature.library.GenreRow
import com.music.app.feature.library.PlaylistRow
import com.music.app.feature.library.R
import com.music.app.feature.library.selection.selectionKeyOf
import com.music.app.core.designsystem.icon.MusicIcons

/**
 * The collections whose plays are kept out of Most played - chosen from each one's options - listed as the library
 * lists them, and let back in many at a time: they are selected as any list's rows are - a long press, then presses -
 * and the selection bar has their plays count again.
 */
data object UncountedCollectionsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: UncountedCollectionsViewModel = hiltViewModel()
        val collections by viewModel.collections.collectAsStateWithLifecycle()
        val picks = rememberSelectionState()
        // The rows select by the library's own keys; each stands for the collection it plays as.
        val pickedParents = collections.filter { selectionKeyOf(it.item)?.let(picks::isSelected) == true }.map { it.parent }

        Column(modifier = Modifier.fillMaxSize()) {
            MusicTopAppBar(
                title = stringResource(R.string.uncounted_title),
                onNavigateBack = navigator::back,
                selection = picks.toTopBarSelection(
                    actions = listOf(
                        picks.selectAllAction(collections.mapNotNull { selectionKeyOf(it.item) }),
                        TopBarAction(label = stringResource(R.string.uncounted_count_again), icon = MusicIcons.TrendingUp) {
                            viewModel.countAgain(pickedParents)
                            picks.clear()
                        },
                    ),
                    onMoreOptions = null,
                ),
            )
            if (collections.isEmpty()) {
                MusicEmptyState(
                    title = stringResource(R.string.uncounted_empty_title),
                    message = stringResource(R.string.uncounted_empty_message),
                )
                return@Column
            }
            val listState = rememberLazyListState()
            FastScroller(listState, Modifier.fillMaxSize()) { overscrollEffect ->
                LazyColumn(
                    state = listState,
                    overscrollEffect = overscrollEffect,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current),
                ) {
                    items(collections, key = { it.parent.toStorageKey() }) { collection ->
                        // Nothing to open from here: a press only joins a selection already running.
                        val open = {}
                        when (val item = collection.item) {
                            is Album -> AlbumRow(item, picks, isCurrent = { false }, isPlaying = { false }, onClick = open, onOpenOptions = null)
                            is Artist -> ArtistRow(item, picks, isCurrent = { false }, isPlaying = { false }, onClick = open, onOpenOptions = null)
                            is Genre -> GenreRow(item, picks, isCurrent = { false }, isPlaying = { false }, onClick = open, onOpenOptions = null)
                            is Playlist ->
                                PlaylistRow(item, picks, isCurrent = { false }, isPlaying = { false }, onClick = open, onOpenOptions = null)
                            is Folder -> FolderRow(item, picks, isCurrent = { false }, isPlaying = { false }, onClick = open, onOpenOptions = null)
                        }
                    }
                }
            }
        }
    }
}
