package com.music.app.feature.download.tools

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import com.music.app.core.designsystem.component.LocalBottomContentPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.designsystem.component.MusicActionButtonGroup
import com.music.app.core.designsystem.component.fab.floatingActionButtonFloor
import com.music.app.core.designsystem.component.MusicEmptyState
import com.music.app.core.designsystem.component.actionButton
import com.music.app.core.designsystem.component.copyToClipboard
import com.music.app.core.navigation.Screen
import com.music.app.feature.download.DownloadLogs
import com.music.app.feature.download.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * What a download said as it ran - every line the downloader wrote and Music's own steps, growing as it goes - YTDLnis's
 * download log: read, copied or shared, to see what went wrong.
 */
data class DownloadLogScreen(val downloadId: Long) : Screen {
    override val titleRes: Int get() = R.string.download_log_title

    // Its Copy and Share at its foot are its own: no mini player over them.
    override val hasPlayer: Boolean get() = false

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val viewModel: DownloadLogViewModel = hiltViewModel()
        val log by remember(downloadId) { viewModel.follow(downloadId) }.collectAsStateWithLifecycle("")
        val lines = remember(log) { log.lines().dropLastWhile(String::isEmpty) }
        val copiedLabel = stringResource(R.string.download_log_copied)
        val copyLabel = stringResource(R.string.download_log_copy)
        val shareLabel = stringResource(R.string.download_log_share)

        Column(modifier = Modifier.fillMaxSize()) {
            if (lines.isEmpty()) {
                MusicEmptyState(
                    title = stringResource(R.string.download_log_empty),
                    message = stringResource(R.string.download_log_empty_message),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            } else {
                // Its end meets the bar below, which stands clear of the navigation bar itself.
                CompositionLocalProvider(LocalBottomContentPadding provides 0.dp) { DownloaderOutput(lines, Modifier.weight(1f)) }
            }
            MusicActionButtonGroup(
                modifier = Modifier.fillMaxWidth().floatingActionButtonFloor().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                actionButton(label = copyLabel, enabled = lines.isNotEmpty(), onClick = {
                    context.copyToClipboard(log, copiedLabel)
                })
                actionButton(label = shareLabel, enabled = lines.isNotEmpty(), onClick = {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, log), null))
                })
            }
        }
    }
}

@HiltViewModel
internal class DownloadLogViewModel @Inject constructor(private val logs: DownloadLogs) : ViewModel() {
    fun follow(id: Long) = logs.follow(id)
}
