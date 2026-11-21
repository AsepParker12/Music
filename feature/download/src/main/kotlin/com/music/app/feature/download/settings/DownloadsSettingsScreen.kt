package com.music.app.feature.download.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.component.settings.SettingsList
import com.music.app.core.designsystem.component.settings.SettingsNavigationItem
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.download.R
import com.music.app.feature.download.tools.CommandsScreen
import com.music.app.feature.download.tools.TerminalScreen
import com.music.app.feature.download.tools.WatchesScreen

/**
 * Downloading from links, as Seal lays its settings out: a page for each part of it - the downloader and how downloads run,
 * where they are saved, how they reach their sites, their subtitles - then the downloader's tools. The
 * downloads themselves are on their own screen.
 */
data object DownloadsSettingsScreen : Screen {
    override val titleRes: Int get() = R.string.downloads_settings_screen_title

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        SettingsList {
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_general_title),
                summary = stringResource(R.string.downloads_settings_general_summary),
                icon = MusicIcons.SettingsApplications,
                onClick = { navigator.go(GeneralDownloadSettingsScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_directory_title),
                summary = stringResource(R.string.downloads_settings_directory_summary),
                icon = MusicIcons.Folder,
                onClick = { navigator.go(DownloadDirectorySettingsScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_network_title),
                summary = stringResource(R.string.downloads_settings_network_summary),
                icon = MusicIcons.SignalWifi,
                onClick = { navigator.go(NetworkSettingsScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_browser_title),
                summary = stringResource(R.string.downloads_settings_browser_summary),
                icon = MusicIcons.Language,
                onClick = { navigator.go(BrowserSettingsScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_subtitles_title),
                summary = stringResource(R.string.downloads_settings_subtitles_summary),
                icon = MusicIcons.Subtitles,
                onClick = { navigator.go(SubtitleSettingsScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_commands_title),
                summary = stringResource(R.string.downloads_settings_commands_summary),
                icon = MusicIcons.Terminal,
                onClick = { navigator.go(CommandsScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_watches_title),
                summary = stringResource(R.string.downloads_settings_watches_summary),
                icon = MusicIcons.Subscriptions,
                onClick = { navigator.go(WatchesScreen) },
            )
            SettingsNavigationItem(
                title = stringResource(R.string.downloads_settings_terminal_title),
                summary = stringResource(R.string.downloads_settings_terminal_summary),
                icon = MusicIcons.Code,
                onClick = { navigator.go(TerminalScreen) },
            )
        }
    }
}
