package com.anod.appwatcher.sync

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.anod.appwatcher.App
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.content.DbContentProvider
import info.anodsplace.framework.app.ApplicationContext

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 */
class ManualSyncService : IntentService("ManualSyncService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val syncAdapter = UpdateCheck(ApplicationContext(applicationContext))
            val contentProviderClient = contentResolver.acquireContentProviderClient(DbContentProvider.authority)

            val bundle = Bundle()
            if (!BuildConfig.DEBUG) {
                bundle.putBoolean(UpdateCheck.extrasManual, true)
            }

            val updatesCount = syncAdapter.perform(bundle, contentProviderClient, App.log(this))

            val finishIntent = Intent(UpdateCheck.syncStop)
            finishIntent.putExtra(UpdateCheck.extrasUpdatesCount, updatesCount)
            sendBroadcast(finishIntent)
        }
    }

    companion object {
        private const val ACTION_MANUAL_SYNC = "com.anod.appwatcher.sync.action.SYNC"

        /**
         * Starts this service to perform action Sync. If
         * the service is already performing a task this action will be queued.

         * @see IntentService
         */
        fun startActionSync(context: Context) {
            val intent = Intent(context, ManualSyncService::class.java)
            intent.action = ACTION_MANUAL_SYNC
            context.startService(intent)
        }
    }

}
