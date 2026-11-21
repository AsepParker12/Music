package com.music.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.music.app.R
import com.music.app.core.datastore.LibraryTab
import com.music.app.core.designsystem.component.TopBarAction
import com.music.app.core.designsystem.component.WindowOverlayHost
import com.music.app.core.designsystem.component.fab.FloatingActionButtonStack
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.PlayerOverlay
import com.music.app.feature.download.DownloadScreen
import com.music.app.feature.library.LibraryPagerScreen
import com.music.app.feature.library.LibraryTabRequest
import com.music.app.feature.settings.QuickPlaySourceSetting
import com.music.app.feature.settings.SettingsScreen
import com.music.app.feature.settings.about.AboutScreen

/**
 * [playingTab] opens the library on the tab the queue plays from, at its playing row, the full player over it;
 * [tabRequest] slides it to a tab, as a tap on the tab does.
 */
@Composable
fun AppShell(playerOverlay: PlayerOverlay, playingTab: LibraryTab?, tabRequest: LibraryTabRequest?, modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current

    // The library owns the top bar rather than this Scaffold, because the bar has to become a
    // context bar when a tab has a selection - and only the library knows which tab that is. These
    // are the actions the shell itself contributes.
    val settingsLabel = stringResource(R.string.app_settings)
    val libraryActions = remember(navigator, settingsLabel) {
        listOf(
            TopBarAction(label = settingsLabel, icon = MusicIcons.Settings) { navigator.go(SettingsScreen) },
        )
    }

    // Over the player too, so a screen can lay something over the whole window - the quick play button's menu.
    WindowOverlayHost {
        // The expandable player overlays the whole screen - collapsed, it's just a mini-bar pinned to the
        // bottom; expanded, it covers everything. Each list keeps its own end clear of it, rather than the
        // screen being cut short above it, so rows still scroll behind the mini player.
        playerOverlay.Content(isPrivate = false, expandsPlayer = playingTab != null) {
            // Inside the player, so the screen's FABs stand clear of it.
            FloatingActionButtonStack {
                // No top bar slot and no content insets: the library's own bar handles the status bar
                // inset, so letting the Scaffold add it too would pad the screen twice.
                Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { innerPadding ->
                    LibraryPagerScreen(
                        onChooseQuickPlaySource = { navigator.go(QuickPlaySourceSetting) },
                        onOpenDownloads = { navigator.go(DownloadScreen()) },
                        onOpenAbout = { navigator.go(AboutScreen) },
                        modifier = Modifier.padding(innerPadding),
                        actions = libraryActions,
                        playingTab = playingTab,
                        tabRequest = tabRequest,
                    )
                }
            }
        }
    }
}
