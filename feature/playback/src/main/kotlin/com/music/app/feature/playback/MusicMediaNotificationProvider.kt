package com.music.app.feature.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.music.app.core.common.notification.MusicNotificationChannel
import com.music.app.core.common.notification.MusicNotificationId

/**
 * The media notification, ported from ArchiveTune's `ArchiveTuneMediaNotificationProvider`.
 *
 * It builds nothing itself: media3's [DefaultMediaNotificationProvider] renders the notification
 * from the session, including its media buttons, and this only wraps it to do two things
 * the default cannot. It names the channel and small icon, which is why the status bar shows
 * Music's mark rather than media3's generic glyph. And it replaces the notification's delete intent
 * so that swiping the notification away arrives at [PlaybackService] as an ordinary command,
 * carrying the original intent along so media3's own dismissal still runs afterwards.
 */
@UnstableApi
class MusicMediaNotificationProvider(
    private val context: Context,
    @DrawableRes smallIconResId: Int,
) : MediaNotification.Provider {
    private val delegate =
        DefaultMediaNotificationProvider(
            context,
            { MusicNotificationId.PLAYBACK },
            MusicNotificationChannel.PLAYBACK.id,
            MusicNotificationChannel.PLAYBACK.nameRes,
        ).apply {
            setSmallIcon(smallIconResId)
        }

    override fun createNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback,
    ): MediaNotification {
        val mediaNotification =
            delegate.createNotification(
                mediaSession,
                mediaButtonPreferences,
                actionFactory,
                onNotificationChangedCallback,
            )

        val originalDeleteIntent = mediaNotification.notification.deleteIntent ?: return mediaNotification
        mediaNotification.notification.deleteIntent =
            PendingIntent.getService(
                context,
                mediaNotification.notificationId,
                Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_MEDIA_NOTIFICATION_DISMISSED
                    putExtra(
                        PlaybackService.EXTRA_MEDIA_NOTIFICATION_DELETE_INTENT,
                        originalDeleteIntent,
                    )
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        return mediaNotification
    }

    override fun getNotificationChannelInfo(): MediaNotification.Provider.NotificationChannelInfo = delegate.notificationChannelInfo

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle,
    ): Boolean = delegate.handleCustomCommand(session, action, extras)
}
