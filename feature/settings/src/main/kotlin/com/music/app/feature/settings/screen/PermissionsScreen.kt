package com.music.app.feature.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.music.app.core.common.permission.AppPermission
import com.music.app.core.designsystem.component.permission.rememberPermissionRequest
import com.music.app.core.designsystem.component.settings.SettingsList
import com.music.app.core.designsystem.component.settings.SettingsSection
import com.music.app.core.designsystem.component.settings.SettingsSwitchItem
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.navigation.Screen
import com.music.app.feature.settings.R

/**
 * Every permission Music uses on this device and what it is for - one row each, read from [AppPermission] so
 * this screen lists exactly what the app asks for. Whether each is granted is its switch: on while Music holds
 * it, off while it does not, as every other setting says which way it is set.
 */
data object PermissionsScreen : Screen {
    override val titleRes: Int get() = R.string.permissions_title

    @Composable
    override fun Content() {
        SettingsList {
            SettingsSection(stringResource(R.string.permissions_section)) {
                AppPermission.needed.forEach { permission -> PermissionItem(permission) }
            }
        }
    }
}

/**
 * [permission], and whether Music holds it - read again every time the screen comes back into view, so a change
 * made in Android's own settings shows the moment the user returns. Turned on, it is asked for as
 * [rememberPermissionRequest] asks; turned off, it opens the page where Android lets it be taken back, which
 * an app cannot do itself.
 */
@Composable
private fun PermissionItem(permission: AppPermission) {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(permission.isGranted(context)) }
    LifecycleResumeEffect(permission) {
        isGranted = permission.isGranted(context)
        onPauseOrDispose {}
    }
    val request = rememberPermissionRequest(permission) { isGranted = it }

    SettingsSwitchItem(
        title = stringResource(permission.titleRes),
        summary = stringResource(permission.purposeRes),
        icon = permission.icon,
        checked = isGranted,
        onCheckedChange = { if (isGranted) context.startActivity(permission.settingsIntent(context)) else request() },
    )
}

internal val AppPermission.titleRes: Int
    get() = when (this) {
        AppPermission.STORAGE -> R.string.permission_storage_title
        AppPermission.NOTIFICATIONS -> R.string.permission_notifications_title
        AppPermission.APP_INSTALLS -> R.string.permission_app_installs_title
    }

internal val AppPermission.purposeRes: Int
    get() = when (this) {
        AppPermission.STORAGE -> R.string.permission_storage_purpose
        AppPermission.NOTIFICATIONS -> R.string.permission_notifications_purpose
        AppPermission.APP_INSTALLS -> R.string.permission_app_installs_purpose
    }

private val AppPermission.icon: ImageVector
    get() = when (this) {
        AppPermission.STORAGE -> MusicIcons.Folder
        AppPermission.NOTIFICATIONS -> MusicIcons.Notifications
        AppPermission.APP_INSTALLS -> MusicIcons.SystemUpdate
    }
