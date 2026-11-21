package com.music.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.music.app.core.common.di.ApplicationScope
import com.music.app.core.common.notification.MusicNotifications
import com.music.app.core.data.LibraryRepository
import com.music.app.core.datastore.DownloadSettings
import com.music.app.core.datastore.EffectSettings
import com.music.app.core.datastore.QuickPlaySettings
import com.music.app.core.datastore.SettingsLoader
import com.music.app.core.designsystem.component.cover.MediaThumbnailFetcher
import com.music.app.core.designsystem.effect.MusicEffects
import com.music.app.core.designsystem.icon.MusicIconPack
import com.music.app.feature.backup.BackupSettings
import com.music.app.feature.backup.work.BackupWork
import com.music.app.feature.download.work.DownloadWork
import com.music.app.feature.feedback.CrashReports
import com.music.app.feature.tageditor.lyrics.MissingLyricsFinder
import com.music.app.feature.update.UpdateMonitor
import com.music.app.widget.PlaybackWidgets
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class MusicApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var settingsLoader: SettingsLoader

    // Providers, for what reads settings as it is built - the repository, and everything built on it: Hilt injects
    // these fields before onCreate has loaded the settings, so each is built only when asked for, after that.
    @Inject
    lateinit var libraryRepository: Provider<LibraryRepository>

    @Inject
    lateinit var missingLyricsFinder: Provider<MissingLyricsFinder>

    @Inject
    lateinit var playbackWidgets: Provider<PlaybackWidgets>

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var effectSettings: EffectSettings

    @Inject
    lateinit var quickPlaySettings: QuickPlaySettings

    @Inject
    lateinit var updateMonitor: UpdateMonitor

    @Inject
    lateinit var backupSettings: BackupSettings

    @Inject
    lateinit var crashReports: CrashReports

    @Inject
    lateinit var downloadSettings: DownloadSettings

    /** Coil's own loader, drawing a track's or a video's cover from Android's thumbnail of its file - see [MediaThumbnailFetcher]. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(MediaThumbnailFetcher.Factory) }
            .build()

    override fun onCreate() {
        super.onCreate()
        // Before anything else can crash, so even a crash as the app starts is there to be reported next time.
        crashReports.install()
        // Before any icon is drawn: an icon pack's drawables replace the icons they are named for.
        MusicIconPack.install(this)
        // First, and blocking: every setting is in memory before the first activity, the playback
        // service or a media button is created, so each of them starts with the user's choices
        // already in place - the way it would start with defaults - instead of correcting itself
        // once they arrive. The files are small and load concurrently, once per process.
        runBlocking { settingsLoader.load() }
        // Before anything can post: every channel, named in the current language, in one system call.
        MusicNotifications.createChannels(this)
        followEffectSettings()
        // Touching the repository here starts its eager database read at the earliest moment the
        // process has a Context - typically well before the first activity is created, and always
        // before the first frame. By the time anything asks for the track list, the query has
        // either finished or is already in flight; nothing waits for it to be started on demand.
        libraryRepository.get()
        // Updates are looked for from here on, whichever activity is open, and in the background as the
        // settings have it: what the last session left is put back first - see UpdateMonitor.
        updateMonitor.start()
        // The tracks around the one playing are given the lyrics their files lack, whichever screen is open.
        missingLyricsFinder.get().start()
        // A widget on the home screen follows playback from the moment the process starts, whatever started it.
        playbackWidgets.get().takeIf { it.isPlaced }?.start()
        // Automatic backups are made exactly as often as the user chose.
        applicationScope.launch { backupSettings.schedule.collect { BackupWork.follow(this@MusicApplication, it) } }
        // yt-dlp kept up to date as often as the user chose, and watched channels looked at, whether or not the download screen
        // is ever opened.
        applicationScope.launch {
            downloadSettings.preferences.flow.map { it.ytDlpAutoUpdate }.distinctUntilChanged()
                .collect { DownloadWork.followYtDlpUpdates(this@MusicApplication, it) }
        }
        DownloadWork.followMaintenance(this)
        // The launcher shortcut is named and drawn for how quick play plays, so it follows that as it changes.
        // Calls to the system's shortcut service, so off the main thread as well.
        applicationScope.launch { quickPlaySettings.mode.flow.collect { QuickPlayShortcut.publish(this@MusicApplication, it) } }
    }

    /**
     * The one place the stored effect settings reach [MusicEffects], which every screen, animation and
     * haptic reads: set before the first frame, then kept in step as the user changes them.
     */
    private fun followEffectSettings() {
        MusicEffects.areAnimationsDisabled = effectSettings.disableAnimations.value
        MusicEffects.isHighRefreshRateForced = effectSettings.forceHighRefreshRate.value
        MusicEffects.areHapticsEnabled = effectSettings.hapticsEnabled.value
        MusicEffects.areScreenshotsBlocked = effectSettings.blockScreenshots.value
        applicationScope.launch { effectSettings.disableAnimations.flow.collect { MusicEffects.areAnimationsDisabled = it } }
        applicationScope.launch { effectSettings.forceHighRefreshRate.flow.collect { MusicEffects.isHighRefreshRateForced = it } }
        applicationScope.launch { effectSettings.hapticsEnabled.flow.collect { MusicEffects.areHapticsEnabled = it } }
        applicationScope.launch { effectSettings.blockScreenshots.flow.collect { MusicEffects.areScreenshotsBlocked = it } }
    }
}
