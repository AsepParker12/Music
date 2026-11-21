package com.music.app.feature.library

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import com.music.app.core.common.format.formatDuration
import com.music.app.core.designsystem.component.InfoSeparator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.designsystem.component.SelectionState
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.feature.library.R
import com.music.app.feature.library.options.OptionsSheet
import com.music.app.feature.library.options.OptionsTarget

@Composable
fun TracksScreen(
    viewModel: LibraryViewModel,
    selection: SelectionState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val trackSections by viewModel.trackSections.collectAsStateWithLifecycle()
    val trackLyricLines by viewModel.trackLyricLines.collectAsStateWithLifecycle()
    var optionsTarget by remember { mutableStateOf<OptionsTarget.ForTrack?>(null) }

    LibraryList(
        content = tracks,
        selection = selection,
        hasPermission = hasPermission,
        isScanning = isScanning,
        emptyTitle = stringResource(R.string.library_no_tracks_found),
        emptyMessage = searchEmptyMessage(searchQuery),
        key = { it.id },
        modifier = modifier,
        listState = listState,
        onRefresh = rememberLibraryRefresh(viewModel),
        sectionOf = trackSections,
        isCurrent = { playback.marks(it) },
        isPlaying = { playback.isPlaying },
    ) { track ->
        TrackRow(
            track = track,
            // Lambdas, so changing songs recomposes two rows instead of the whole list.
            isCurrent = { playback.marks(track) },
            isPlaying = { playback.isPlaying },
            selection = selection,
            onClick = { viewModel.onTrackClick(track) },
            onOpenOptions = {
                optionsTarget = OptionsTarget.ForTrack(track, queueSource = viewModel.tabTracksOf(track))
            },
            // A track the search found by its lyrics alone says why, with the line it found.
            subtitle = trackLyricLines[track.id]?.let { line -> "♪ $line" }
                // The library's own line for a track: who it is by, and how long it is.
                ?: "${track.artist}$InfoSeparator${formatDuration(track.durationMs)}",
        )
    }

    optionsTarget?.let { target ->
        OptionsSheet(target = target, onDismissRequest = { optionsTarget = null })
    }
}
