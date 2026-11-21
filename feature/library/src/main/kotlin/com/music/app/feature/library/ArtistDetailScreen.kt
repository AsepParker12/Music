package com.music.app.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.designsystem.component.CoverArtDefaults
import com.music.app.core.designsystem.component.InfoSeparator
import com.music.app.core.designsystem.component.MusicArtistCover
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.library.R
import com.music.app.feature.library.options.ArtistOptionsContext
import com.music.app.feature.library.options.OptionsTarget
import com.music.app.feature.library.options.TrackOptionsContext

data class ArtistDetailScreen(val artistId: Long) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<ArtistDetailViewModel, ArtistDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(artistId) },
        )
        val artist by viewModel.artist.collectAsStateWithLifecycle()
        val tracks by viewModel.tracks.collectAsStateWithLifecycle()
        val genreNames by viewModel.genreNames.collectAsStateWithLifecycle()

        TrackListDetail(
            title = artist?.name ?: stringResource(R.string.options_type_artist),
            header = DetailHeaderContent(
                type = stringResource(R.string.options_type_artist),
                subhead = genreNames.joinToString(", ").ifEmpty { null },
                info = listOf(
                    countLabel(R.plurals.library_albums, artist?.albumCount ?: 0),
                    trackCountLabel(tracks.itemsOrEmpty.size),
                ).joinToString(InfoSeparator),
                cover = {
                    MusicArtistCover(
                        coverArtUris = artist?.coverArtUris.orEmpty(),
                        seed = artistId.hashCode(),
                        size = CoverArtDefaults.DetailHeaderSize,
                    )
                },
            ),
            onBack = navigator::back,
            viewModel = viewModel,
            emptyMessage = stringResource(R.string.artist_empty),
            collection = artist?.let { OptionsTarget.ForArtist(it, ArtistOptionsContext.FROM_DETAIL) },
            trackOptionsContext = TrackOptionsContext.FROM_ARTIST,
            // Every track here is the artist's, so each is told apart by its album - as Auxio's are.
            trackSubtitle = { it.album },
        )
    }
}
