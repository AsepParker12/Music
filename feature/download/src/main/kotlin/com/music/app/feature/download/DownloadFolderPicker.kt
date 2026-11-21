package com.music.app.feature.download

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.music.app.core.common.permission.AppPermission
import com.music.app.core.common.storage.storageFolderPathOf
import com.music.app.core.designsystem.component.actionButton
import com.music.app.core.designsystem.component.dialog.MusicDialog
import com.music.app.core.designsystem.component.toast

/** Download: the one folder Android lets an app save every kind of file in by itself. */
private const val DownloadFolder = "Download"

/**
 * Picks a folder anywhere on the device's storage - Android's own folder picker, where one can be made too - handed to
 * [onPick] as folders are kept: see `DEFAULT_DOWNLOAD_FOLDER`. Outside Download, Android lets an app save there only once it
 * manages all files: where Music does not yet, the folder is kept only once the user goes to allow it. What is returned
 * opens the picker.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberDownloadFolderPicker(onPick: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    var waitingOnAccess by remember { mutableStateOf<String?>(null) }
    val notOnDevice = stringResource(R.string.download_folder_not_on_device)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val path = storageFolderPathOf(uri) ?: return@rememberLauncherForActivityResult context.toast(notOnDevice)
        if (isInDownload(path) || AppPermission.STORAGE.isGranted(context)) onPick(path) else waitingOnAccess = path
    }

    waitingOnAccess?.let { path ->
        val cancelLabel = stringResource(R.string.download_cancel)
        val allowLabel = stringResource(R.string.download_folder_access_allow)
        val close = { waitingOnAccess = null }
        MusicDialog(
            onDismissRequest = close,
            title = stringResource(R.string.download_folder_access_title),
            buttons = {
                actionButton(label = cancelLabel, onClick = close)
                actionButton(label = allowLabel, onClick = {
                    onPick(path)
                    runCatching { context.startActivity(AppPermission.STORAGE.settingsIntent(context)) }
                    close()
                })
            },
        ) {
            Text(stringResource(R.string.download_folder_access_message, path), style = MaterialTheme.typography.bodyMedium)
        }
    }
    return { picker.launch(null) }
}

/** Whether [path] is Download, or within it. */
private fun isInDownload(path: String): Boolean = path == DownloadFolder || path.startsWith("$DownloadFolder/")
