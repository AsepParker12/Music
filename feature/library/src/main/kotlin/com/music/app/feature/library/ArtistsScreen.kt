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
fun ArtistsScreen(
    viewModel: LibraryViewModel,
    selection: SelectionState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val artistSections by viewModel.artistSections.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var optionsTarget by remember { mutableStateOf<OptionsTarget.ForArtist?>(null) }

    LibraryList(
        content = artists,
        selection = selection,
        hasPermission = hasPermission,
        isScanning = isScanning,
        emptyTitle = stringResource(R.string.library_no_artists_found),
        emptyMessage = searchEmptyMessage(searchQuery),
        key = { it.id },
        sectionOf = artistSections,
        isCurrent = { playback.marks(it) },
        isPlaying = { playback.isPlaying },
        modifier = modifier,
        listState = listState,
        onRefresh = rememberLibraryRefresh(viewModel),
    ) { artist ->
        ArtistRow(
            artist = artist,
            selection = selection,
            isCurrent = { playback.marks(artist) },
            isPlaying = { playback.isPlaying },
            onClick = { navigator.go(ArtistDetailScreen(artist.id)) },
            onOpenOptions = { optionsTarget = OptionsTarget.ForArtist(artist) },
        )
    }

    optionsTarget?.let { target ->
        OptionsSheet(target = target, onDismissRequest = { optionsTarget = null })
    }
}
