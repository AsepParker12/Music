package com.music.app.feature.library

import androidx.lifecycle.viewModelScope
import com.music.app.core.data.LibraryContent
import com.music.app.core.data.LibraryRepository
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.data.sort.LibrarySortOrders
import com.music.app.core.model.Genre
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = GenreDetailViewModel.Factory::class)
class GenreDetailViewModel @AssistedInject constructor(
    @Assisted genreId: Long,
    repository: LibraryRepository,
    sortOrders: LibrarySortOrders,
    playbackController: PlaybackController,
) : TrackListDetailViewModel(playbackController, repository) {

    @AssistedFactory
    interface Factory {
        fun create(genreId: Long): GenreDetailViewModel
    }

    override val playbackParent: PlaybackParent = PlaybackParent.Genre(genreId)

    val genre: StateFlow<Genre?> = repository.genre(genreId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override val tracks: StateFlow<LibraryContent<Track>> = repository.genreTracks(genreId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryContent.Loading)

    override val sort: SortControl = sortOrders.control(SortableList.GENRE_TRACKS, genreId.toString())

    /** The artists with tracks in the genre, by name - Auxio's `GENRE_ARTIST_SORT`. */
    override val sections: StateFlow<List<DetailSection>> = combine(repository.listedArtists, tracks) { artists, tracks ->
        val artistIds = tracks.itemsOrEmpty.flatMapTo(HashSet()) { it.artistIds }
        val genreArtists = artists.itemsOrEmpty
            .filter { it.id in artistIds }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        listOfNotNull(genreArtists.takeIf { it.isNotEmpty() }?.let(DetailSection::Artists))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
