package com.anod.appwatcher.accounts

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.R
import info.anodsplace.context.ApplicationContext
import info.anodsplace.notification.NotificationManager

class DeviceRegistrationNotification(
    private val context: ApplicationContext,
    private val notificationManager: NotificationManager
) {
    companion object {
        internal const val NOTIFICATION_ID = 3
        const val AUTHENTICATION_CHANNEL_ID = "authentication"
    }

    fun show() {
        val intent = Intent(context.actual, AppWatcherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentIntent = PendingIntent.getActivity(
            context.actual,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val description = context.getString(R.string.device_registration_required_description)
        val notification = NotificationCompat.Builder(context.actual, AUTHENTICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.device_registration_required))
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}