@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.music.app.feature.player.style

import com.music.app.feature.player.PlayerBarEdgeGap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.designsystem.player.PlayerBarButton
import com.music.app.core.designsystem.player.PlayerButtonGroup
import com.music.app.core.designsystem.player.formatPlaybackTime
import com.music.app.core.designsystem.player.playerButton
import com.music.app.core.designsystem.player.playerTextButton
import com.music.app.feature.player.PlayerBarButtonSize
import com.music.app.feature.player.PlayerHorizontalPadding
import com.music.app.feature.player.R
import com.music.app.feature.player.background.PlayerColors

/**
 * The default player's bottom row, which the queue sheet is dragged up by: the queue at its start, and the sleep timer and
 * the lyrics - on while they are open - together at its end. The sleep timer says the time it has left while it runs.
 */
@Composable
internal fun DefaultQueueBar(
    colors: PlayerColors,
    isLyricsOpen: Boolean,
    sleepTimerEnabled: Boolean,
    sleepTimerTimeLeft: Long,
    onToggleLyrics: () -> Unit,
    onExpandQueue: () -> Unit,
    onSleepTimerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The bar's buttons are their own touch targets: Material would otherwise pad each out past the row's keyline.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides PlayerBarButtonSize) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(horizontal = PlayerHorizontalPadding, vertical = PlayerBarEdgeGap)
                    .height(PlayerBarButtonSize),
        ) {
            PlayerBarButton(MusicIcons.QueueMusic, stringResource(R.string.player_queue), colors.content, onExpandQueue)
            Spacer(Modifier.weight(1f))
            val sleepTimerLabel = stringResource(R.string.player_sleep_timer)
            val lyricsLabel = stringResource(R.string.player_lyrics)
            PlayerButtonGroup {
                // Running, the time it has left in its button's place, on - tapped, it stops.
                if (sleepTimerEnabled) {
                    playerTextButton(formatPlaybackTime(sleepTimerTimeLeft), sleepTimerLabel, colors.content, checked = true, onClick = onSleepTimerClick)
                } else {
                    playerButton(MusicIcons.Bedtime, sleepTimerLabel, colors.content, onSleepTimerClick)
                }
                playerButton(MusicIcons.Lyrics, lyricsLabel, colors.content, onToggleLyrics, checked = isLyricsOpen)
            }
        }
    }
}
