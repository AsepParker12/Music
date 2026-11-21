package com.music.app.core.common.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.music.app.core.common.R

/**
 * The one way the app posts a notification: every channel made up front, every notification started
 * from the same builder - so each carries Music's own icon in the status bar, and opens the app where its
 * channel says - and posted only where Android would show it.
 */
object MusicNotifications {

    /**
     * Creates every [MusicNotificationChannel], or renames one to the current language. Called as the app
     * starts, before anything can post, in a single call to the system.
     */
    fun createChannels(context: Context) {
        val channels = MusicNotificationChannel.entries.map { channel ->
            NotificationChannelCompat.Builder(channel.id, channel.importance)
                .setName(context.getString(channel.nameRes))
                .setDescription(context.getString(channel.descriptionRes))
                // A badge on the launcher is for what asks to be noticed, not for work in progress.
                .setShowBadge(channel.importance >= NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .build()
        }
        NotificationManagerCompat.from(context).createNotificationChannelsCompat(channels)
    }

    /** Music's icon, every notification's in the status bar. */
    @DrawableRes
    val icon: Int = R.drawable.ic_stat_music

    /** A notification on [channel], with Music's icon, opening the app at the channel's entry - where every notification the app posts starts. */
    fun builder(context: Context, channel: MusicNotificationChannel): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(icon)
            .setContentIntent(channel.entry.pendingIntent(context))

    /**
     * [builder], as the platform's own - for what only it has: progress promoted to a live update, on Android 16
     * and later, where [canPostPromoted].
     */
    fun platformBuilder(context: Context, channel: MusicNotificationChannel): Notification.Builder =
        Notification.Builder(context, channel.id)
            .setSmallIcon(icon)
            .setContentIntent(channel.entry.pendingIntent(context))

    /** Whether Android shows Music's progress as live updates - on Android 16 and later, while the user allows it. */
    fun canPostPromoted(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            context.getSystemService(NotificationManager::class.java).canPostPromotedNotifications()

    /**
     * A task running, on [channel]: [title], what it is doing now - [text] - and [detail] beneath, with how far it is -
     * [percent], null while it cannot tell - and [actions]. On Android 16 and later, where [canPostPromoted] and
     * [asLiveUpdate], a promoted live update, its percent the title, which the system shows in the status bar too; a
     * progress bar otherwise - which, unlike a live update, Android bundles with the app's others. [since], when the task began,
     * is kept as the notification's time: posted again as it goes, it stays where it is among the others, which Android orders
     * by it, rather than coming first each time.
     */
    fun progress(
        context: Context,
        channel: MusicNotificationChannel,
        title: String,
        text: String?,
        detail: String?,
        percent: Int?,
        actions: List<MusicNotificationAction>,
        asLiveUpdate: Boolean = true,
        since: Long? = null,
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && asLiveUpdate && canPostPromoted(context)) {
            val progressStyle = Notification.ProgressStyle()
                .setProgressPoints(listOf(Notification.ProgressStyle.Point(0), Notification.ProgressStyle.Point(PROGRESS_MAX)))
                .setProgress(percent ?: 0)
                .setProgressIndeterminate(percent == null)
            return platformBuilder(context, channel)
                .setContentTitle(percent?.let { context.getString(R.string.notification_progress_percent, it) } ?: title)
                .setContentText(text ?: title)
                .setSubText(detail ?: title)
                .setStyle(progressStyle)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .apply { since?.let(::setWhen) }
                // Asks for the notification to be promoted to a live update.
                .addExtras(Bundle().apply { putBoolean("android.requestPromotedOngoing", true) })
                .apply { actions.forEach { addAction(Notification.Action.Builder(Icon.createWithResource(context, it.iconRes), it.label, it.intent).build()) } }
                .build()
        }
        return builder(context, channel)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(listOfNotNull(text, detail).joinToString("\n")))
            .setProgress(PROGRESS_MAX, percent ?: 0, percent == null)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .apply { since?.let(::setWhen) }
            .apply { actions.forEach { addAction(it.iconRes, it.label, it.intent) } }
            .build()
    }

    /**
     * Posts [notification] as [id], replacing whatever that id showed - unless notifications are turned
     * off for the app, or not yet allowed, when there is nothing to post to.
     */
    @SuppressLint("MissingPermission") // What areNotificationsEnabled() answers, the permission included.
    fun post(context: Context, id: Int, notification: Notification) {
        val manager = NotificationManagerCompat.from(context)
        if (manager.areNotificationsEnabled()) manager.notify(id, notification)
    }

    /** Takes the notification [id] out of the shade. */
    fun cancel(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }
}

/** What a notification offers: [label], drawn as [iconRes], doing [intent]. */
class MusicNotificationAction(val label: String, @DrawableRes val iconRes: Int, val intent: PendingIntent)

private const val PROGRESS_MAX = 100

/**
 * Promotes this service to the foreground, showing [notification] as [id] - a data-sync service, the
 * type every one of Music's is. `startForegroundService()` requires this within seconds of every start.
 */
fun Service.startForegroundCompat(id: Int, notification: Notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    } else {
        startForeground(id, notification)
    }
}
