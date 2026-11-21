package com.music.app.feature.settings

import androidx.compose.ui.graphics.vector.ImageVector
import com.music.app.core.navigation.Screen
import com.music.app.feature.feedback.FeedbackScreen
import com.music.app.feature.settings.about.AboutScreen
import com.music.app.feature.settings.appearance.AppearanceScreen
import com.music.app.feature.settings.backup.BackupScreen
import com.music.app.feature.download.DownloadScreen
import com.music.app.feature.settings.screen.BehaviorScreen
import com.music.app.feature.settings.screen.ContentScreen
import com.music.app.feature.settings.screen.ListeningStatsScreen
import com.music.app.feature.settings.screen.LyricsScreen
import com.music.app.feature.settings.screen.PermissionsScreen
import com.music.app.feature.settings.screen.PlaybackScreen
import com.music.app.feature.settings.screen.StorageScreen
import com.music.app.feature.settings.screen.VideoScreen
import com.music.app.feature.settings.updates.UpdatesScreen
import com.music.app.core.designsystem.icon.MusicIcons

/** The bands the settings home groups its categories into. */
enum class SettingsGroup(val titleRes: Int) {
    LibraryAndPlayback(R.string.settings_group_library_and_playback),
    YourMusic(R.string.settings_group_your_music),
    App(R.string.settings_group_app),
}

/**
 * Every category the settings home offers, in the order it offers them.
 *
 * Declared as data rather than written out as rows, so a category is added in one place instead of
 * two and cannot end up in the list without a destination or with a title that disagrees with its
 * screen's. The home reads this; nothing else needs to know the order.
 */
enum class SettingsCategory(
    val group: SettingsGroup,
    val titleRes: Int,
    val summaryRes: Int,
    val icon: ImageVector,
    val screen: Screen,
) {
    Appearance(
        group = SettingsGroup.LibraryAndPlayback,
        titleRes = R.string.appearance_title,
        summaryRes = R.string.appearance_summary,
        icon = MusicIcons.Palette,
        screen = AppearanceScreen,
    ),
    Playback(
        group = SettingsGroup.LibraryAndPlayback,
        titleRes = R.string.playback_title,
        summaryRes = R.string.playback_summary,
        icon = MusicIcons.Song,
        screen = PlaybackScreen,
    ),
    Video(
        group = SettingsGroup.LibraryAndPlayback,
        titleRes = R.string.video_title,
        summaryRes = R.string.video_summary,
        icon = MusicIcons.VideoLibrary,
        screen = VideoScreen,
    ),
    Lyrics(
        group = SettingsGroup.LibraryAndPlayback,
        titleRes = R.string.lyrics_title,
        summaryRes = R.string.lyrics_summary,
        icon = MusicIcons.Lyrics,
        screen = LyricsScreen,
    ),
    Content(
        group = SettingsGroup.LibraryAndPlayback,
        titleRes = R.string.content_title,
        summaryRes = R.string.content_summary,
        icon = MusicIcons.Language,
        screen = ContentScreen,
    ),
    Behavior(
        group = SettingsGroup.LibraryAndPlayback,
        titleRes = R.string.behavior_title,
        summaryRes = R.string.behavior_summary,
        icon = MusicIcons.TouchApp,
        screen = BehaviorScreen,
    ),
    Downloads(
        group = SettingsGroup.YourMusic,
        titleRes = R.string.downloads_settings_title,
        summaryRes = R.string.downloads_settings_summary,
        icon = MusicIcons.Download,
        // The downloads themselves; their settings are a tap away in the screen's bar.
        screen = DownloadScreen(),
    ),
    ListeningStats(
        group = SettingsGroup.YourMusic,
        titleRes = R.string.listening_stats_title,
        summaryRes = R.string.listening_stats_summary,
        icon = MusicIcons.ShowChart,
        screen = ListeningStatsScreen,
    ),
    Permissions(
        group = SettingsGroup.App,
        titleRes = R.string.permissions_title,
        summaryRes = R.string.permissions_summary,
        icon = MusicIcons.Security,
        screen = PermissionsScreen,
    ),
    Storage(
        group = SettingsGroup.App,
        titleRes = R.string.storage_title,
        summaryRes = R.string.storage_summary,
        icon = MusicIcons.Storage,
        screen = StorageScreen,
    ),
    Backup(
        group = SettingsGroup.App,
        titleRes = R.string.backup_title,
        summaryRes = R.string.backup_summary,
        icon = MusicIcons.Backup,
        screen = BackupScreen,
    ),
    Updates(
        group = SettingsGroup.App,
        titleRes = R.string.updates_title,
        summaryRes = R.string.updates_summary,
        icon = MusicIcons.SystemUpdate,
        screen = UpdatesScreen,
    ),
    Feedback(
        group = SettingsGroup.App,
        titleRes = R.string.feedback_category_title,
        summaryRes = R.string.feedback_category_summary,
        icon = MusicIcons.Feedback,
        screen = FeedbackScreen(),
    ),
    About(
        group = SettingsGroup.App,
        titleRes = R.string.about_title,
        summaryRes = R.string.about_summary,
        icon = MusicIcons.Info,
        screen = AboutScreen,
    ),
}
