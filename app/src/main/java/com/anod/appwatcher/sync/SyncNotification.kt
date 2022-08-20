package com.anod.appwatcher.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.NotificationActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.watchlist.WatchListActivity
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.NotificationManager
import info.anodsplace.framework.text.Html

/**
 * @author alex
 * *
 * @date 2014-09-24
 */
class SyncNotification(private val context: ApplicationContext, private val notificationManager: NotificationManager) {

    companion object {
        internal const val syncNotificationId = 1
        internal const val gmsNotificationId = 2
        const val updatesChannelId = "versions_updates"
        const val pricesChannelId = "prices_change"
        const val authenticationId = "authentication"
    }

    class Filter(
            private val filterInstalled: Boolean,
            private val filterInstalledUpToDate: Boolean,
            private val filterNoChanges: Boolean) {

        constructor(prefs: Preferences)
                : this(!prefs.isNotifyInstalled, !prefs.isNotifyInstalledUpToDate, !prefs.isNotifyNoChanges)

        val hasFilters: Boolean
            get() = (filterInstalled || filterInstalledUpToDate || filterNoChanges)

        fun apply(updatedApps: List<UpdatedApp>): List<UpdatedApp> {
            return updatedApps.filter append@{
                if (filterInstalled) {
                    if (it.installedVersionCode > 0) {
                        return@append false
                    }
                } else if (filterInstalledUpToDate) {
                    if (it.installedVersionCode > 0 && it.versionNumber <= it.installedVersionCode) {
                        return@append false
                    }
                }
                if (filterNoChanges) {
                    if (it.noNewDetails) {
                        return@append false
                    }
                }
                true
            }
        }
    }

    fun createChannels() {
        val updates = NotificationChannel(updatesChannelId, context.getString(R.string.channel_app_updates), NotificationManager.IMPORTANCE_DEFAULT)
        updates.description = context.getString(R.string.channel_updates_description)
        updates.setShowBadge(true)

        val prices = NotificationChannel(pricesChannelId, context.getString(R.string.channel_prices), NotificationManager.IMPORTANCE_DEFAULT)
        prices.description = context.getString(R.string.channel_prices_description)
        prices.setShowBadge(true)

        val authentication = NotificationChannel(authenticationId, context.getString(R.string.channel_authentication), NotificationManager.IMPORTANCE_DEFAULT)
        prices.description = context.getString(R.string.channel_authentication_description)
        prices.setShowBadge(true)
        notificationManager.createNotificationChannels(listOf(updates, prices, authentication))
    }

    fun show(updatedApps: List<UpdatedApp>) {

        val sorted = updatedApps.sortedWith(compareBy({ it.isNewUpdate }, { it.title }))

        val notification = this.create(sorted)
        notificationManager.notify(syncNotificationId, notification)
    }

    fun cancel() {
        notificationManager.cancel(syncNotificationId)
    }

    private fun create(updatedApps: List<UpdatedApp>): Notification {
        val notificationIntent = Intent(context.actual, AppWatcherActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val data = Uri.parse("com.anod.appwatcher://notification")
        notificationIntent.data = data
        notificationIntent.putExtra(WatchListActivity.EXTRA_FROM_NOTIFICATION, true)
        val contentIntent = PendingIntent.getActivity(context.actual, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

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
            addSingleExtraInfo(app, builder)
        } else {
            addMultipleExtraInfo(updatedApps, builder)
        }

        return builder.build()
    }

    private fun addMultipleExtraInfo(updatedApps: List<UpdatedApp>, builder: NotificationCompat.Builder) {
        updatedApps.firstOrNull { it.installedVersionCode > 0 && it.versionNumber > it.installedVersionCode }
                ?: return

        val bigText = updatedApps.joinToString(",\n") { it.title }
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))

        val updateIntent = NotificationActivity.intent(
                Uri.parse("com.anod.appwatcher://play/myapps/1"),
                NotificationActivity.actionMyApps,
                context.actual)
        builder.addAction(R.drawable.ic_system_update_alt_white_24dp, context.getString(R.string.noti_action_update),
                PendingIntent.getActivity(context.actual, 0, updateIntent, PendingIntent.FLAG_IMMUTABLE)
        )

        val readIntent = NotificationActivity.intent(
                Uri.parse("com.anod.appwatcher://dismiss/"),
                NotificationActivity.actionDismiss,
                context.actual
        )
        builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.dismiss),
                PendingIntent.getActivity(context.actual, 0, readIntent, PendingIntent.FLAG_IMMUTABLE)
        )
    }

    private fun addSingleExtraInfo(update: UpdatedApp, builder: NotificationCompat.Builder) {

        val changes = update.recentChanges.ifBlank { context.getString(R.string.no_recent_changes) }

        builder.setContentText(Html.parse(changes))
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(Html.parse(changes)))

        val playIntent = NotificationActivity.intent(
                Uri.parse("com.anod.appwatcher://play/" + update.packageName),
                NotificationActivity.actionPlayStore,
                context.actual).also {
            it.putExtra(NotificationActivity.extraPackage, update.packageName)
        }

        builder.addAction(R.drawable.ic_play_arrow_white_24dp, context.getString(R.string.store),
                PendingIntent.getActivity(context.actual, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
        )

        if (update.installedVersionCode > 0) {
            val updateIntent = NotificationActivity.intent(
                    Uri.parse("com.anod.appwatcher://play/myapps/1"),
                    NotificationActivity.actionMyApps,
                    context.actual)
            builder.addAction(R.drawable.ic_system_update_alt_white_24dp, context.getString(R.string.noti_action_update),
                    PendingIntent.getActivity(context.actual, 0, updateIntent, PendingIntent.FLAG_IMMUTABLE)
            )
        }

        val readIntent = NotificationActivity.intent(
                Uri.parse("com.anod.appwatcher://viewed/"),
                NotificationActivity.actionMarkViewed,
                context.actual)
        builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.dismiss),
                PendingIntent.getActivity(context.actual, 0, readIntent, PendingIntent.FLAG_IMMUTABLE)
        )
    }

    private fun renderText(apps: List<UpdatedApp>): String {
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

    private fun renderTitle(apps: List<UpdatedApp>): String {
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