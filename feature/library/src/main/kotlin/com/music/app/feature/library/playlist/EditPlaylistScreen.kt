package com.music.app.feature.library.playlist

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.common.cover.rankedCoverArtUris
import com.music.app.core.data.LibraryContent
import com.music.app.core.data.playlist.coverArtUris
import com.music.app.core.designsystem.component.CoverArtDefaults
import com.music.app.core.designsystem.component.fastscroll.FastScroller
import com.music.app.core.designsystem.component.LocalBottomContentPadding
import com.music.app.core.designsystem.component.MusicPlaylistCover
import com.music.app.core.designsystem.component.MusicTopAppBar
import com.music.app.core.designsystem.component.MusicTrackRow
import com.music.app.core.designsystem.component.TopBarAction
import com.music.app.core.designsystem.component.TopBarSearch
import com.music.app.core.designsystem.component.section.ColumnSection
import com.music.app.core.designsystem.component.section.rememberSectionListState
import com.music.app.core.designsystem.component.section.section
import com.music.app.core.designsystem.component.title
import com.music.app.core.designsystem.component.toast
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.model.PlaylistCover
import com.music.app.core.model.Track
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.library.LibraryListContent
import com.music.app.feature.library.R
import com.music.app.feature.library.isPlaylistNameTaken
import com.music.app.feature.library.options.OptionsAction
import com.music.app.feature.library.options.label
import com.music.app.feature.library.searchEmptyMessage
import com.music.app.feature.library.searchedFor
import androidx.compose.foundation.text.input.TextFieldLineLimits
import com.music.app.core.designsystem.component.rememberSyncedTextFieldState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization

/**
 * Editing the playlist [playlistId]: its name and its cover, previewed as they are changed and saved
 * together - back leaves both as they were.
 *
 * The cover is the stacked covers of its tracks by default, or one image: its first or last track in
 * its current sort, followed as that changes; one track from anywhere in the library; or an image from
 * the device. Favorites takes a cover like any other playlist, but keeps its name.
 */
data class EditPlaylistScreen(val playlistId: Long) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        val viewModel = hiltViewModel<EditPlaylistViewModel, EditPlaylistViewModel.Factory>(
            creationCallback = { factory -> factory.create(playlistId) },
        )
        val playlist by viewModel.playlist.collectAsStateWithLifecycle()
        val takenNames by viewModel.takenNames.collectAsStateWithLifecycle()
        val playlistTracks by viewModel.playlistTracks.collectAsStateWithLifecycle()
        val tracksById by viewModel.tracksById.collectAsStateWithLifecycle()
        var isChoosingTrack by rememberSaveable { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }

        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) viewModel.chooseImage(uri.toString())
        }

        if (isChoosingTrack) {
            val libraryTracks by viewModel.libraryTracks.collectAsStateWithLifecycle()
            CoverTrackChooser(
                playlistTracks = playlistTracks,
                libraryTracks = libraryTracks,
                onTrackChosen = { track ->
                    viewModel.chooseTrack(track)
                    isChoosingTrack = false
                },
                onBack = { isChoosingTrack = false },
            )
            return
        }

        val saved = playlist
        val isRenamable = saved?.isBuiltIn == false
        val isNameTaken = isPlaylistNameTaken(viewModel.name, takenNames)
        val canSave = viewModel.isDraftReady && saved != null && !isSaving &&
            viewModel.name.isNotBlank() && !isNameTaken &&
            (viewModel.name.trim() != saved.name || viewModel.cover != saved.cover)

        val chosenTrack = viewModel.chosenTrackId?.let(tracksById::get)
        val stackedCoverArtUris = remember(playlistTracks) { rankedCoverArtUris(playlistTracks.map { it.coverArtUri }) }
        fun coverArtUrisOf(cover: PlaylistCover) =
            cover.coverArtUris(stackedCoverArtUris, playlistTracks, chosenTrack?.coverArtUri)

        Column(modifier = Modifier.fillMaxSize()) {
            MusicTopAppBar(
                title = OptionsAction.EDIT.label,
                onNavigateBack = navigator::back,
                actions = listOf(
                    TopBarAction(label = stringResource(R.string.edit_playlist_save), icon = MusicIcons.Check, enabled = canSave) {
                        isSaving = true
                        viewModel.save { succeeded ->
                            isSaving = false
                            if (succeeded) navigator.close() else context.toast(context.getString(R.string.edit_playlist_save_failed))
                        }
                    },
                ),
            )
            if (!viewModel.isDraftReady) return@Column

            val scrollState = rememberScrollState()
            // Above the keyboard, so the field it opens for is scrolled into view rather than the window slid.
            FastScroller(scrollState, Modifier.fillMaxSize().imePadding()) { overscrollEffect ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState, overscrollEffect = overscrollEffect)
                        .padding(bottom = LocalBottomContentPadding.current),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MusicPlaylistCover(
                        coverArtUris = coverArtUrisOf(viewModel.cover),
                        seed = playlistId.hashCode(),
                        size = CoverArtDefaults.DetailHeaderSize,
                        cornerRadius = CoverArtDefaults.DetailHeaderCornerRadius,
                        modifier = Modifier.padding(16.dp),
                    )
                    OutlinedTextField(
                        state = rememberSyncedTextFieldState(if (isRenamable) viewModel.name else saved?.title.orEmpty(), viewModel::rename),
                        label = { Text(stringResource(R.string.edit_playlist_name)) },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        enabled = isRenamable,
                        isError = isNameTaken,
                        supportingText = when {
                            !isRenamable -> ({ Text(stringResource(R.string.edit_playlist_name_fixed, saved?.title.orEmpty())) })
                            isNameTaken -> ({ Text(stringResource(R.string.playlist_name_taken)) })
                            else -> null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    ColumnSection(title = stringResource(R.string.edit_playlist_cover)) {
                        Column(modifier = Modifier.selectableGroup()) {
                            CoverOptionRow(
                                label = stringResource(R.string.edit_playlist_cover_stacked),
                                description = stringResource(R.string.edit_playlist_cover_stacked_summary),
                                coverArtUris = stackedCoverArtUris,
                                seed = playlistId.hashCode(),
                                selected = viewModel.cover == PlaylistCover.Stacked,
                                onClick = { viewModel.selectCover(PlaylistCover.Stacked) },
                            )
                            CoverOptionRow(
                                label = stringResource(R.string.edit_playlist_cover_first),
                                description = playlistTracks.firstOrNull()?.title ?: stringResource(R.string.edit_playlist_no_tracks),
                                coverArtUris = coverArtUrisOf(PlaylistCover.FirstTrack),
                                seed = playlistId.hashCode(),
                                selected = viewModel.cover == PlaylistCover.FirstTrack,
                                onClick = { viewModel.selectCover(PlaylistCover.FirstTrack) },
                            )
                            CoverOptionRow(
                                label = stringResource(R.string.edit_playlist_cover_last),
                                description = playlistTracks.lastOrNull()?.title ?: stringResource(R.string.edit_playlist_no_tracks),
                                coverArtUris = coverArtUrisOf(PlaylistCover.LastTrack),
                                seed = playlistId.hashCode(),
                                selected = viewModel.cover == PlaylistCover.LastTrack,
                                onClick = { viewModel.selectCover(PlaylistCover.LastTrack) },
                            )
                            // Choosing again once chosen, or with nothing chosen yet, opens the choice; otherwise
                            // the track or image chosen before is simply selected again.
                            val isTrackSelected = viewModel.cover is PlaylistCover.OfTrack
                            CoverOptionRow(
                                label = stringResource(R.string.edit_playlist_cover_track),
                                description = chosenTrack?.title ?: stringResource(R.string.edit_playlist_cover_track_summary),
                                coverArtUris = chosenTrack?.coverArtUri?.let(::listOf).orEmpty(),
                                seed = playlistId.hashCode(),
                                selected = isTrackSelected,
                                onClick = {
                                    if (isTrackSelected || chosenTrack == null) {
                                        isChoosingTrack = true
                                    } else {
                                        viewModel.selectCover(PlaylistCover.OfTrack(chosenTrack.id))
                                    }
                                },
                            )
                            val chosenImageUri = viewModel.chosenImageUri
                            val isImageSelected = viewModel.cover is PlaylistCover.Image
                            CoverOptionRow(
                                label = stringResource(R.string.edit_playlist_cover_image),
                                description = stringResource(if (chosenImageUri == null) R.string.edit_playlist_cover_image_summary else R.string.edit_playlist_cover_image_chosen),
                                coverArtUris = chosenImageUri?.let(::listOf).orEmpty(),
                                seed = playlistId.hashCode(),
                                selected = isImageSelected,
                                onClick = {
                                    if (isImageSelected || chosenImageUri == null) {
                                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    } else {
                                        viewModel.selectCover(PlaylistCover.Image(chosenImageUri))
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}


/** One cover the playlist can take: a preview of it, what it is, and whether it is the one chosen. */
@Composable
private fun CoverOptionRow(
    label: String,
    description: String,
    coverArtUris: List<String>,
    seed: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MusicPlaylistCover(coverArtUris = coverArtUris, seed = seed, size = CoverArtDefaults.ListSize)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RadioButton(selected = selected, onClick = null)
    }
}

/**
 * Choosing the one track whose cover the playlist takes: its own tracks first, then the whole library,
 * both narrowed by a search. Back closes the search first, then the chooser.
 */
@Composable
private fun CoverTrackChooser(
    playlistTracks: List<Track>,
    libraryTracks: LibraryContent<Track>,
    onTrackChosen: (Track) -> Unit,
    onBack: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val sections = rememberSectionListState()
    BackHandler(onBack = onBack)

    Column(modifier = Modifier.fillMaxSize()) {
        MusicTopAppBar(
            title = stringResource(R.string.edit_playlist_choose_track),
            onNavigateBack = onBack,
            actions = listOf(TopBarAction(label = stringResource(R.string.library_search), icon = MusicIcons.Search) { searchQuery = "" }),
            search = searchQuery?.let { query ->
                TopBarSearch(query = query, onQueryChange = { searchQuery = it }, onClose = { searchQuery = null })
            },
        )
        val visiblePlaylistTracks = playlistTracks.searchedFor(searchQuery) { listOf(it.title, it.artist, it.album) }
        LibraryListContent(
            content = libraryTracks.searchedFor(searchQuery) { listOf(it.title, it.artist, it.album) },
            // Reached from a playlist, so the library has already loaded and been permitted.
            hasPermission = true,
            isScanning = false,
            emptyTitle = stringResource(R.string.library_no_tracks_found),
            emptyMessage = searchEmptyMessage(searchQuery),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { visibleLibraryTracks ->
            // Read here, for the list's builder, which runs outside composition.
            val inPlaylistTitle = stringResource(R.string.edit_playlist_in_playlist)
            val allTracksTitle = stringResource(R.string.edit_playlist_all_tracks)
            FastScroller(listState = listState, modifier = Modifier.fillMaxSize()) { overscrollEffect ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current),
                    overscrollEffect = overscrollEffect,
                ) {
                    val libraryRows: LazyListScope.() -> Unit = {
                        items(items = visibleLibraryTracks, key = { "library-${it.id}" }) { track ->
                            CoverTrackRow(track = track, onClick = { onTrackChosen(track) })
                        }
                    }
                    // Sectioned only while the playlist has tracks to show: the library alone needs no heading.
                    if (visiblePlaylistTracks.isNotEmpty()) {
                        section(key = "playlist", title = inPlaylistTitle, state = sections) {
                            items(items = visiblePlaylistTracks, key = { "playlist-${it.id}" }) { track ->
                                CoverTrackRow(track = track, onClick = { onTrackChosen(track) })
                            }
                        }
                        section(key = "library", title = allTracksTitle, state = sections, hasDividerAbove = true, content = libraryRows)
                    } else {
                        libraryRows()
                    }
                }
            }
        }
    }
}

/** A track to choose: the library's own row, with nothing to select, play or open options for. */
@Composable
private fun CoverTrackRow(track: Track, onClick: () -> Unit) {
    MusicTrackRow(
        track = track,
        isCurrent = { false },
        isPlaying = { false },
        selection = null,
        selectionKey = null,
        onClick = onClick,
        onOpenOptions = null,
    )
}
