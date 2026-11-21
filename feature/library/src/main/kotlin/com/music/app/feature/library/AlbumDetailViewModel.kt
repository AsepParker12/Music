package com.music.app.feature.library

import androidx.lifecycle.viewModelScope
import com.music.app.core.data.LibraryContent
import com.music.app.core.data.LibraryRepository
import com.music.app.core.data.sort.LibrarySortOrders
import com.music.app.core.model.Album
import com.music.app.core.model.PlaybackParent
import com.music.app.core.model.Track
import com.music.app.core.model.sort.SortableList
import com.music.app.feature.library.sort.SortControl
import com.music.app.feature.library.sort.control
import com.music.app.feature.playback.PlaybackController
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = AlbumDetailViewModel.Factory::class)
class AlbumDetailViewModel @AssistedInject constructor(
    @Assisted albumId: Long,
    repository: LibraryRepository,
    sortOrders: LibrarySortOrders,
    playbackController: PlaybackController,
) : TrackListDetailViewModel(playbackController, repository) {

    @AssistedFactory
    interface Factory {
        fun create(albumId: Long): AlbumDetailViewModel
    }

    override val playbackParent: PlaybackParent = PlaybackParent.Album(albumId)

    val album: StateFlow<Album?> = repository.album(albumId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override val tracks: StateFlow<LibraryContent<Track>> = repository.albumTracks(albumId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryContent.Loading)

    override val sort: SortControl = sortOrders.control(SortableList.ALBUM_TRACKS, albumId.toString())
}
