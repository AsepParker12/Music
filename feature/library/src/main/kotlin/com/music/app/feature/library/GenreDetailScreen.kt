package com.music.app.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.designsystem.component.CoverArtDefaults
import com.music.app.core.designsystem.component.InfoSeparator
import com.music.app.core.designsystem.component.MusicGenreCover
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.library.R
import com.music.app.feature.library.options.GenreOptionsContext
import com.music.app.feature.library.options.OptionsTarget

data class GenreDetailScreen(val genreId: Long) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<GenreDetailViewModel, GenreDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(genreId) },
        )
        val genre by viewModel.genre.collectAsStateWithLifecycle()
        val tracks by viewModel.tracks.collectAsStateWithLifecycle()

        TrackListDetail(
            title = genre?.name ?: stringResource(R.string.options_type_genre),
            header = DetailHeaderContent(
                type = stringResource(R.string.options_type_genre),
                subhead = null,
                info = listOf(
                    countLabel(R.plurals.library_artists, genre?.artistCount ?: 0),
                    trackCountLabel(tracks.itemsOrEmpty.size),
                ).joinToString(InfoSeparator),
                cover = {
                    MusicGenreCover(
                        coverArtUris = genre?.coverArtUris.orEmpty(),
                        seed = genreId.hashCode(),
                        size = CoverArtDefaults.DetailHeaderSize,
                        cornerRadius = CoverArtDefaults.DetailHeaderCornerRadius,
                    )
                },
            ),
            onBack = navigator::back,
            viewModel = viewModel,
            emptyMessage = stringResource(R.string.genre_empty),
            collection = genre?.let { OptionsTarget.ForGenre(it, GenreOptionsContext.FROM_DETAIL) },
        )
    }
}
