package com.anod.appwatcher.sync

import android.content.Context
import androidx.work.*
import com.anod.appwatcher.Application
import info.anodsplace.framework.AppLog
import java.util.concurrent.TimeUnit

/**
 * @author Alex Gavrishev
 * *
 * @date 27/05/2016.
 */

object SyncScheduler {
    private const val tag = "AppRefresh"

    fun schedule(requiresCharging: Boolean, requiresWifi: Boolean, windowStartSec: Long) {

        val constraints: Constraints = Constraints.Builder().apply {
            setRequiresCharging(requiresCharging)
            if (requiresWifi) {
                setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }
        }.build()

        val request: PeriodicWorkRequest =
                PeriodicWorkRequestBuilder<SyncWorker>(windowStartSec, TimeUnit.SECONDS)
                        .setInputData(workDataOf())
                        .setConstraints(constraints)
                        .build()

        AppLog.i("Schedule sync in ${windowStartSec/3600} hours")
        WorkManager.getInstance().enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    fun cancel() {
        WorkManager.getInstance().cancelUniqueWork(tag)
    }
}
