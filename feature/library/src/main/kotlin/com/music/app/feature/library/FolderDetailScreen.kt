package com.music.app.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.designsystem.component.CoverArtDefaults
import com.music.app.core.designsystem.component.MusicFolderCover
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.library.R
import com.music.app.feature.library.options.FolderOptionsContext
import com.music.app.feature.library.options.OptionsTarget

/** The folder at [folderPath]: its music - or, where [isVideo], its videos, as the Videos tab opens it. */
data class FolderDetailScreen(val folderPath: String, val isVideo: Boolean) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<FolderDetailViewModel, FolderDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(folderPath, isVideo) },
        )
        val folder by viewModel.folder.collectAsStateWithLifecycle()
        val tracks by viewModel.tracks.collectAsStateWithLifecycle()

        TrackListDetail(
            title = viewModel.folderName,
            header = DetailHeaderContent(
                type = stringResource(R.string.options_type_folder),
                subhead = folderPath,
                info = trackCountAndDuration(tracks.itemsOrEmpty),
                cover = {
                    MusicFolderCover(
                        coverArtUris = folder?.coverArtUris.orEmpty(),
                        seed = folderPath.hashCode(),
                        size = CoverArtDefaults.DetailHeaderSize,
                        cornerRadius = CoverArtDefaults.DetailHeaderCornerRadius,
                    )
                },
            ),
            onBack = navigator::back,
            viewModel = viewModel,
            emptyMessage = stringResource(if (isVideo) R.string.folder_empty_videos else R.string.folder_empty_tracks),
            collection = folder?.let { OptionsTarget.ForFolder(it, FolderOptionsContext.FROM_DETAIL) },
        )
    }
}
