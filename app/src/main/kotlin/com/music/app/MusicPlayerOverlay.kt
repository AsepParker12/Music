package com.music.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.PlayerOverlay
import com.music.app.feature.equalizer.EqualizerScreen
import com.music.app.feature.library.AlbumDetailScreen
import com.music.app.feature.library.ArtistDetailScreen
import com.music.app.feature.library.PlayingList
import com.music.app.feature.library.playingListOf
import com.music.app.feature.library.options.OptionsSheet
import com.music.app.feature.library.options.OptionsTarget
import com.music.app.feature.playback.PlaybackSpace
import com.music.app.feature.playback.VideoPlayback
import com.music.app.feature.player.BottomSheetPlayerHost
import com.music.app.feature.tageditor.lyricseditor.LyricsEditorScreen
import com.music.app.feature.vault.VaultTrackOptionsSheet
import com.music.app.feature.video.openVideoPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * The player laid over every activity - the library's, or over a Private Folder screen the folder's, each with
 * the options its tracks have: the library's album and artist screens, the list its queue plays from, its lyrics
 * editor and its options sheet; the folder's own options.
 *
 * Whichever activity is in front opens the video player as a video starts being watched - see
 * [VideoPlayback.openings] - so it opens wherever a video was started from, and only once.
 */
class MusicPlayerOverlay @Inject constructor(
    private val videoPlayback: VideoPlayback,
) : PlayerOverlay {

    @Composable
    override fun Content(isPrivate: Boolean, expandsPlayer: Boolean, content: @Composable () -> Unit) {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                videoPlayback.openings.collect(context::openVideoPlayer)
            }
        }

        if (isPrivate) {
            BottomSheetPlayerHost(
                space = PlaybackSpace.Private,
                onGoToAlbum = null,
                onGoToArtist = null,
                onGoToPlayingFrom = null,
                onOpenEqualizer = { navigator.go(EqualizerScreen) },
                onEditLyrics = null,
                trackOptionsSheet = { track, onDismissRequest -> VaultTrackOptionsSheet(track, onDismissRequest) },
                expandsOnOpen = expandsPlayer,
                content = content,
            )
        } else {
            BottomSheetPlayerHost(
                space = PlaybackSpace.Library,
                onGoToAlbum = { albumId -> navigator.go(AlbumDetailScreen(albumId)) },
                onGoToArtist = { artistId -> navigator.go(ArtistDetailScreen(artistId)) },
                // A collection's own screen, or the library's tab for the whole library and every video.
                onGoToPlayingFrom = { parent ->
                    when (val list = playingListOf(parent)) {
                        is PlayingList.OnScreen -> navigator.go(list.screen)
                        is PlayingList.OnTab -> context.startActivity(MainActivity.libraryTabIntent(context, list.tab))
                    }
                },
                onOpenEqualizer = { navigator.go(EqualizerScreen) },
                // Over the lyrics sheet, which is still open on the way back, with the lyrics as saved.
                onEditLyrics = { trackId -> navigator.go(LyricsEditorScreen(trackId)) },
                // The player and its queue open the same options sheet every track row in the library
                // opens - the app is where the two features are introduced to each other.
                trackOptionsSheet = { track, onDismissRequest ->
                    OptionsSheet(
                        target = OptionsTarget.ForTrack(track),
                        onDismissRequest = onDismissRequest,
                    )
                },
                expandsOnOpen = expandsPlayer,
                content = content,
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerOverlayModule {
    @Binds
    abstract fun bindPlayerOverlay(overlay: MusicPlayerOverlay): PlayerOverlay
}
