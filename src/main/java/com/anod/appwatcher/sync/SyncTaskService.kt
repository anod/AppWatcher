package com.anod.appwatcher.sync

import android.content.Intent
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.content.DbContentProvider
import com.google.android.gms.gcm.*
import info.anodsplace.android.log.AppLog

class SyncTaskService : GcmTaskService() {

    override fun onInitializeTasks() {
        super.onInitializeTasks()
        val prefs = Preferences(applicationContext)
        if (prefs.useAutoSync()) {
            SyncScheduler.schedule(applicationContext, prefs.isRequiresCharging)
        }
    }

    override fun onRunTask(taskParams: TaskParams): Int {
        AppLog.d("Scheduled call executed. Task: " + taskParams.tag)

        val syncAdapter = SyncAdapter(applicationContext)
        val contentProviderClient = contentResolver.acquireContentProviderClient(DbContentProvider.AUTHORITY)

        val updatesCount = syncAdapter.onPerformSync(taskParams.extras, contentProviderClient)

        val finishIntent = Intent(SyncAdapter.SYNC_STOP)
        finishIntent.putExtra(SyncAdapter.EXTRA_UPDATES_COUNT, updatesCount)
        sendBroadcast(finishIntent)

        return GcmNetworkManager.RESULT_SUCCESS
    }
}
