package com.music.app.feature.library

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.designsystem.component.SelectionState
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.navigation.LocalNavigator
import com.music.app.feature.library.R
import com.music.app.feature.library.options.OptionsSheet
import com.music.app.feature.library.options.OptionsTarget

@Composable
fun AlbumsScreen(
    viewModel: LibraryViewModel,
    selection: SelectionState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val albumSections by viewModel.albumSections.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var optionsTarget by remember { mutableStateOf<OptionsTarget.ForAlbum?>(null) }

    LibraryList(
        content = albums,
        selection = selection,
        hasPermission = hasPermission,
        isScanning = isScanning,
        emptyTitle = stringResource(R.string.library_no_albums_found),
        emptyMessage = searchEmptyMessage(searchQuery),
        key = { it.id },
        sectionOf = albumSections,
        isCurrent = { playback.marks(it) },
        isPlaying = { playback.isPlaying },
        modifier = modifier,
        listState = listState,
        onRefresh = rememberLibraryRefresh(viewModel),
    ) { album ->
        AlbumRow(
            album = album,
            selection = selection,
            isCurrent = { playback.marks(album) },
            isPlaying = { playback.isPlaying },
            onClick = { navigator.go(AlbumDetailScreen(album.id)) },
            onOpenOptions = { optionsTarget = OptionsTarget.ForAlbum(album) },
        )
    }

    optionsTarget?.let { target ->
        OptionsSheet(target = target, onDismissRequest = { optionsTarget = null })
    }
}
