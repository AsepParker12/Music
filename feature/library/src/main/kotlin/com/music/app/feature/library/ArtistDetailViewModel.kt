package com.music.app.feature.library

import androidx.lifecycle.viewModelScope
import com.music.app.core.data.LibraryContent
import com.music.app.core.data.LibraryRepository
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.data.sort.LibrarySortOrders
import com.music.app.core.model.Artist
import com.music.app.core.model.PlaybackParent
import com.music.app.core.model.Track
import com.music.app.core.model.sort.SortableList
import com.music.app.feature.library.R
import com.music.app.feature.library.sort.SortControl
import com.music.app.feature.library.sort.control
import com.music.app.feature.playback.PlaybackController
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = ArtistDetailViewModel.Factory::class)
class ArtistDetailViewModel @AssistedInject constructor(
    @Assisted artistId: Long,
    repository: LibraryRepository,
    sortOrders: LibrarySortOrders,
    playbackController: PlaybackController,
) : TrackListDetailViewModel(playbackController, repository) {

    @AssistedFactory
    interface Factory {
        fun create(artistId: Long): ArtistDetailViewModel
    }

    override val playbackParent: PlaybackParent = PlaybackParent.Artist(artistId)

    val artist: StateFlow<Artist?> = repository.artist(artistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override val tracks: StateFlow<LibraryContent<Track>> = repository.artistTracks(artistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryContent.Loading)

    override val sort: SortControl = sortOrders.control(SortableList.ARTIST_TRACKS, artistId.toString())

    /**
     * The artist's albums, then the albums it appears on - those holding its tracks under another
     * artist's name - as Auxio's explicit and implicit albums. Music reads no release types, so every
     * album of the artist's own is one "Albums" section rather than Auxio's EPs, singles and so on.
     */
    override val sections: StateFlow<List<DetailSection>> = combine(repository.albums, tracks) { albums, tracks ->
        val albumIdsWithTracks = tracks.itemsOrEmpty.mapTo(HashSet()) { it.albumId }
        val (own, appearances) = albums.itemsOrEmpty
            .filter { artistId in it.artistIds || it.id in albumIdsWithTracks }
            .sortedWith(ArtistAlbumOrder)
            .partition { artistId in it.artistIds }
        listOfNotNull(
            own.takeIf { it.isNotEmpty() }?.let { DetailSection.Albums(R.string.library_albums_title, it) },
            appearances.takeIf { it.isNotEmpty() }?.let { DetailSection.Albums(R.string.detail_appears_on, it) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The genres the artist's tracks are filed under, for the header - Auxio's artist subhead. */
    val genreNames: StateFlow<List<String>> = tracks.map { tracks ->
        tracks.itemsOrEmpty.mapNotNull { it.genre }.distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
