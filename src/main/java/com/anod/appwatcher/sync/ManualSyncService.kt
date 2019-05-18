package com.anod.appwatcher.sync

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.workDataOf
import com.anod.appwatcher.BuildConfig
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 */
class ManualSyncService : IntentService("ManualSyncService") {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val syncAdapter = UpdateCheck(ApplicationContext(applicationContext))

            val data = workDataOf(
                UpdateCheck.extrasManual to !BuildConfig.DEBUG
            )
            coroutineScope.launch {
                val updatesCount = syncAdapter.perform(data)
                val finishIntent = Intent(UpdateCheck.syncStop)
                finishIntent.putExtra(UpdateCheck.extrasUpdatesCount, updatesCount)
                sendBroadcast(finishIntent)
            }
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
