package com.music.app.feature.debug

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.music.app.core.navigation.DebugTools
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.debug.database.DatabasesScreen
import com.music.app.feature.debug.files.FilesScreen
import com.music.app.feature.debug.logs.LogsScreen
import com.music.app.feature.debug.mediastore.MediaStoreScreen
import com.music.app.feature.debug.preferences.PreferencesScreen
import com.music.app.feature.debug.runtime.RuntimeScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.music.app.core.designsystem.component.settings.SettingsActionItem
import com.music.app.core.designsystem.component.settings.SettingsList
import com.music.app.core.designsystem.component.settings.SettingsNavigationItem
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import com.music.app.core.designsystem.icon.MusicIcons

/** Every part of the debug tools: a screen added here is one the tools offer. */
private enum class DebugSection(val title: String, val summary: String, val icon: ImageVector, val screen: Screen) {
    RUNTIME("Runtime", "App, permissions, playback, scanner, backup and background work, live", MusicIcons.Monitor, RuntimeScreen),
    DATABASES("Databases", "Every database: tables, rows and SQL - to read and to change", MusicIcons.Storage, DatabasesScreen),
    PREFERENCES("Preferences", "Every settings file and shared preferences file - to read and to change", MusicIcons.Settings, PreferencesScreen),
    FILES("Files", "Everything the app keeps on storage, caches included - to read and to delete", MusicIcons.Folder, FilesScreen),
    MEDIA_STORE("MediaStore", "What Android itself says of the device's files, before the scanner reads it", MusicIcons.LibraryMusic, MediaStoreScreen),
    LOGS("Logs", "What this process has logged", MusicIcons.Subject, LogsScreen),
}

/** The debug tools' home: everything behind how the app behaves, to look into and to change. */
data object DebugToolsScreen : Screen {
    override fun title(context: Context): String = "Debug tools"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<DebugViewModel>()
        SettingsList {
            DebugSection.entries.forEach { section ->
                SettingsNavigationItem(
                    title = section.title,
                    summary = section.summary,
                    icon = section.icon,
                    onClick = { navigator.go(section.screen) },
                )
            }
            SettingsActionItem(
                title = "Show setup again",
                summary = "Restart the app into the first-launch setup, from its first step",
                icon = MusicIcons.RestartAlt,
                onClick = viewModel::showSetupAgain,
            )
        }
    }
}

internal class MusicDebugTools @Inject constructor() : DebugTools {
    override val screen: Screen = DebugToolsScreen
    override val summary = "Databases, stored data, files, logs and live state"
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DebugToolsModule {
    @Binds
    abstract fun bindDebugTools(tools: MusicDebugTools): DebugTools
}
