package com.music.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.app.core.data.LibraryContent
import com.music.app.core.data.LibraryRepository
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.data.sort.LibrarySortOrders
import com.music.app.core.model.PlaybackParent
import com.music.app.core.model.Playlist
import com.music.app.core.model.Track
import com.music.app.core.model.playbackParent
import com.music.app.core.model.sort.SortableList
import com.music.app.feature.library.R
import com.music.app.feature.library.sort.SortControl
import com.music.app.feature.library.sort.control
import com.music.app.feature.playback.PlaybackController
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = PlaylistDetailViewModel.Factory::class)
class PlaylistDetailViewModel @AssistedInject constructor(
    @Assisted private val playlistId: Long,
    private val repository: LibraryRepository,
    sortOrders: LibrarySortOrders,
    playbackController: PlaybackController,
) : TrackListDetailViewModel(playbackController, repository) {

    override val sort: SortControl = sortOrders.control(SortableList.PLAYLIST_TRACKS, playlistId.toString())

    @AssistedFactory
    interface Factory {
        fun create(playlistId: Long): PlaylistDetailViewModel
    }

    override val playbackParent: PlaybackParent = PlaybackParent.Playlist(playlistId)

    val playlist: StateFlow<Playlist?> = repository.playlists
        .map { content -> content.itemsOrEmpty.firstOrNull { it.id == playlistId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override val tracks: StateFlow<LibraryContent<Track>> = repository.playlistTracks(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryContent.Loading)

    /**
     * Favorites lists, above its tracks, the collections favorited as themselves - a section per kind, each
     * there once it holds one, the latest favorited first - one tap from each. A collection no longer in the
     * library is not listed, and is again once it is back. Any other playlist lists its tracks alone.
     */
    override val sections: StateFlow<List<DetailSection>> =
        if (playlistId != repository.favoritesPlaylistId) {
            super.sections
        } else {
            combine(
                repository.favoriteCollections,
                repository.artists,
                repository.albums,
                repository.genres,
                combine(repository.folders, repository.videoFolders, repository.playlists, ::Triple),
            ) { favorites, artists, albums, genres, (folders, videoFolders, playlists) ->
                fun <T, K> favorited(items: List<T>, keyOf: (T) -> K, parentOf: (PlaybackParent) -> K?): List<T> {
                    val byKey = items.associateBy(keyOf)
                    return favorites.mapNotNull { parent -> parentOf(parent)?.let(byKey::get) }
                }
                listOfNotNull(
                    favorited(artists.itemsOrEmpty, { it.id }, { (it as? PlaybackParent.Artist)?.artistId })
                        .takeIf { it.isNotEmpty() }?.let(DetailSection::Artists),
                    favorited(albums.itemsOrEmpty, { it.id }, { (it as? PlaybackParent.Album)?.albumId })
                        .takeIf { it.isNotEmpty() }?.let { DetailSection.Albums(R.string.library_albums_title, it) },
                    favorited(genres.itemsOrEmpty, { it.id }, { (it as? PlaybackParent.Genre)?.genreId })
                        .takeIf { it.isNotEmpty() }?.let(DetailSection::Genres),
                    favorited(folders.itemsOrEmpty + videoFolders.itemsOrEmpty, { it.playbackParent }, { it as? PlaybackParent.Folder })
                        .takeIf { it.isNotEmpty() }?.let(DetailSection::Folders),
                    favorited(playlists.itemsOrEmpty, { it.id }, { (it as? PlaybackParent.Playlist)?.playlistId })
                        .takeIf { it.isNotEmpty() }?.let(DetailSection::Playlists),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        }
}

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    repository: LibraryRepository,
    playbackController: PlaybackController,
) : TrackListDetailViewModel(playbackController, repository) {

    override val playbackParent: PlaybackParent = PlaybackParent.RecentlyPlayed

    override val tracks: StateFlow<LibraryContent<Track>> = repository.recentlyPlayedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryContent.Loading)
}

@HiltViewModel
class MostPlayedViewModel @Inject constructor(
    repository: LibraryRepository,
    playbackController: PlaybackController,
) : TrackListDetailViewModel(playbackController, repository) {

    override val playbackParent: PlaybackParent = PlaybackParent.MostPlayed

    override val tracks: StateFlow<LibraryContent<Track>> = repository.mostPlayedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryContent.Loading)
}
