package com.anod.appwatcher.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.NotificationActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.WatchListActivity
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.text.Html

/**
 * @author alex
 * *
 * @date 2014-09-24
 */
class SyncNotification(private val context: ApplicationContext) {

    companion object {
        internal const val syncNotificationId = 1
        internal const val gpsNotificationId = 2
        const val updatesChannelId = "versions_updates"
        const val pricesChannelId = "prices_change"
        const val authenticationId = "authentication"
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val updates = NotificationChannel(updatesChannelId, context.getString(R.string.channel_app_updates), NotificationManager.IMPORTANCE_DEFAULT)
        updates.description = context.getString(R.string.channel_updates_description)
        updates.setShowBadge(true)

        val prices = NotificationChannel(pricesChannelId, context.getString(R.string.channel_prices), NotificationManager.IMPORTANCE_DEFAULT)
        prices.description = context.getString(R.string.channel_prices_description)
        prices.setShowBadge(true)

        val authentication = NotificationChannel(authenticationId, context.getString(R.string.channel_authentication), NotificationManager.IMPORTANCE_DEFAULT)
        prices.description = context.getString(R.string.channel_authentication_description)
        prices.setShowBadge(true)
        context.notificationManager.createNotificationChannels(listOf(updates, prices, authentication))
    }

    fun show(updatedApps: List<UpdateCheck.UpdatedApp>) {

        val sorted = updatedApps.sortedWith(compareBy({ it.isNewUpdate }, { it.title }))

        val notification = this.create(sorted)
        val notificationManager = context.notificationManager
        notificationManager.notify(syncNotificationId, notification)
    }

    fun cancel() {
        val notificationManager = context.notificationManager
        notificationManager.cancel(syncNotificationId)
    }

    private fun create(updatedApps: List<UpdateCheck.UpdatedApp>): Notification {
        val notificationIntent = Intent(context.actual, AppWatcherActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val data = Uri.parse("com.anod.appwatcher://notification")
        notificationIntent.data = data
        notificationIntent.putExtra(WatchListActivity.EXTRA_FROM_NOTIFICATION, true)
        val contentIntent = PendingIntent.getActivity(context.actual, 0, notificationIntent, 0)

        val title = renderTitle(updatedApps)
        val text = renderText(updatedApps)

        val builder = NotificationCompat.Builder(context.actual, updatesChannelId)
        builder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setTicker(title)
                .color = context.getColor(R.color.material_blue_800)

        if (updatedApps.size == 1) {
            val app = updatedApps[0]
            addExtraInfo(app, builder)
        } else {
            addMultipleExtraInfo(updatedApps, builder)
        }

        return builder.build()
    }

    private fun addMultipleExtraInfo(updatedApps: List<UpdateCheck.UpdatedApp>, builder: NotificationCompat.Builder) {
        updatedApps.firstOrNull { it.installedVersionCode > 0 && it.versionNumber > it.installedVersionCode }
                ?: return

        val bigText = updatedApps.joinToString(",\n") { it.title }
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))

        val updateIntent = createActionIntent(Uri.parse("com.anod.appwatcher://play/myapps/1"), NotificationActivity.TYPE_MYAPPS_UPDATE)
        builder.addAction(R.drawable.ic_system_update_alt_white_24dp, context.getString(R.string.noti_action_update),
                PendingIntent.getActivity(context.actual, 0, updateIntent, 0)
        )

        val readIntent = createActionIntent(Uri.parse("com.anod.appwatcher://dismiss/"), NotificationActivity.TYPE_DISMISS)
        builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.dismiss),
                PendingIntent.getActivity(context.actual, 0, readIntent, 0)
        )
    }

    private fun addExtraInfo(update: UpdateCheck.UpdatedApp, builder: NotificationCompat.Builder) {

        val changes = if (update.recentChanges.isBlank()) context.getString(R.string.no_recent_changes) else update.recentChanges

        builder.setContentText(Html.parse(changes))
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(Html.parse(changes)))

        val playIntent = createActionIntent(Uri.parse("com.anod.appwatcher://play/" + update.packageName), NotificationActivity.TYPE_PLAY)
        playIntent.putExtra(NotificationActivity.EXTRA_PKG, update.packageName)

        builder.addAction(R.drawable.ic_play_arrow_white_24dp, context.getString(R.string.store),
                PendingIntent.getActivity(context.actual, 0, playIntent, 0)
        )

        if (update.installedVersionCode > 0) {
            val updateIntent = createActionIntent(Uri.parse("com.anod.appwatcher://play/myapps/1"), NotificationActivity.TYPE_MYAPPS_UPDATE)
            builder.addAction(R.drawable.ic_system_update_alt_white_24dp, context.getString(R.string.noti_action_update),
                    PendingIntent.getActivity(context.actual, 0, updateIntent, 0)
            )
        }

        val readIntent = createActionIntent(Uri.parse("com.anod.appwatcher://dismiss/"), NotificationActivity.TYPE_DISMISS)
        builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.dismiss),
                PendingIntent.getActivity(context.actual, 0, readIntent, 0)
        )
    }

    private fun createActionIntent(uri: Uri, type: Int): Intent {
        val intent = Intent(context.actual, NotificationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data = uri
        intent.putExtra(NotificationActivity.EXTRA_TYPE, type)
        return intent
    }

    private fun renderText(apps: List<UpdateCheck.UpdatedApp>): String {
        val count = apps.size
        if (count == 1) {
            return context.getString(R.string.notification_click)
        }
        if (count > 2) {
            return context.getString(
                    R.string.notification_2_apps_more,
                    apps[0].title,
                    apps[1].title
            )
        }
        return context.getString(R.string.notification_2_apps,
                apps[0].title,
                apps[1].title
        )
    }

    private fun renderTitle(apps: List<UpdateCheck.UpdatedApp>): String {
        val title: String
        val count = apps.size
        title = if (count == 1) {
            context.getString(R.string.notification_one_updated, apps[0].title)
        } else {
            context.getString(R.string.notification_many_updates, count)
        }
        return title
    }

}
