package com.music.app.feature.update.notification

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.music.app.core.common.notification.MusicNotificationChannel
import com.music.app.core.common.notification.MusicNotificationId
import com.music.app.core.common.notification.MusicNotifications
import com.music.app.feature.update.R
import com.music.app.feature.update.UpdateService
import com.music.app.feature.update.github.Release
import com.music.app.feature.update.installedBuild

/**
 * The update notification - ArchiveTune's `UpdateNotificationManager`: one per version, opening the Updates
 * screen. Either a new version is out, with a Download action that starts the in-app download straight away
 * ([notifyAvailable]) - or it is already downloaded, and waits only for the user to install it ([notifyReady]).
 */
object UpdateNotifier {
    private const val PREFS = "update_notifier"
    private const val KEY_LAST_NOTIFIED_VERSION = "last_notified_version"
    private const val KEY_LAST_READY_VERSION = "last_ready_version"

    /** Takes the notification down - what turning update notifications off does. */
    fun dismiss(context: Context) {
        MusicNotifications.cancel(context, MusicNotificationId.UPDATE_AVAILABLE)
    }

    /** Notifies of [release] - once: a version already announced is not announced again. */
    internal fun notifyAvailable(context: Context, release: Release) {
        notifyOnce(context, KEY_LAST_NOTIFIED_VERSION, release.versionName) {
            setContentText(context.getString(R.string.update_notification_text, release.versionName))
            if (release.apkFor(context.installedBuild()) != null) {
                addAction(
                    R.drawable.ic_update,
                    context.getString(R.string.update_notification_download),
                    PendingIntent.getForegroundService(
                        context,
                        1,
                        UpdateService.startIntent(context, release),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                )
            }
        }
    }

    /** Notifies that [versionName] is downloaded and needs the user to install it - once, as [notifyAvailable]. */
    internal fun notifyReady(context: Context, versionName: String) {
        notifyOnce(context, KEY_LAST_READY_VERSION, versionName) {
            setContentText(context.getString(R.string.update_notification_ready_text, versionName))
        }
    }

    private fun notifyOnce(context: Context, key: String, versionName: String, content: NotificationCompat.Builder.() -> Unit) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(key, null) == versionName) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val notification = MusicNotifications.builder(context, MusicNotificationChannel.UPDATE_ALERTS)
            .setContentTitle(context.getString(R.string.update_notification_title))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply(content)
            .build()
        MusicNotifications.post(context, MusicNotificationId.UPDATE_AVAILABLE, notification)
        prefs.edit { putString(key, versionName) }
    }
}
