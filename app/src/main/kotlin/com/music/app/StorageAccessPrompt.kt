package com.music.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.music.app.core.common.permission.AppPermission
import com.music.app.core.designsystem.component.actionButton
import com.music.app.core.designsystem.component.dialog.MusicDialog
import com.music.app.core.designsystem.component.permission.rememberPermissionRequest

/**
 * Asks for access to the device's files as the app opens, where Android grants it only from a settings page of
 * its own: says what it is for first, rather than sending the user out of the app unannounced. Once each time the
 * app is opened without it - where Android asks in a dialog instead, the launch asks there.
 */
@Composable
fun StorageAccessPrompt() {
    val context = LocalContext.current
    val permission = AppPermission.STORAGE
    var isOffered by rememberSaveable { mutableStateOf(permission.runtimePermission == null && !permission.isGranted(context)) }
    val requestAccess = rememberPermissionRequest(permission)
    if (!isOffered) return

    val cancelLabel = stringResource(android.R.string.cancel)
    val allowLabel = stringResource(R.string.storage_access_allow)
    MusicDialog(
        onDismissRequest = { isOffered = false },
        title = stringResource(R.string.storage_access_title),
        buttons = {
            actionButton(label = cancelLabel, onClick = { isOffered = false })
            actionButton(label = allowLabel, onClick = {
                isOffered = false
                requestAccess()
            })
        },
    ) {
        Text(stringResource(R.string.storage_access_message))
    }
}
