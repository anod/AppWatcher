package com.anod.appwatcher.sync

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.work.*
import com.anod.appwatcher.Application
import com.anod.appwatcher.BuildConfig
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import java.util.concurrent.TimeUnit

/**
 * @author Alex Gavrishev
 * *
 * @date 27/05/2016.
 */

class SyncScheduler(private val context: ApplicationContext) {
    constructor(context: Context) : this(ApplicationContext(context))

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
                PeriodicWorkRequestBuilder<SyncWorker>(windowStartSec, TimeUnit.SECONDS, PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS)
                        .setInputData(workDataOf())
                        .setConstraints(constraints)
                        .build()

        AppLog.i("Schedule sync in ${windowStartSec/3600} hours")
        WorkManager.getInstance(context.actual).enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, request)
    }
    
    fun execute() {
        val constraints: Constraints = Constraints.Builder().apply {
            setRequiresCharging(false)
            setRequiredNetworkType(NetworkType.CONNECTED)
            if (Build.VERSION.SDK_INT >= 23) setRequiresDeviceIdle(false)
        }.build()

        val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(workDataOf(UpdateCheck.extrasManual to !BuildConfig.DEBUG))
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context.actual).enqueue(request)
    }

    fun cancel() {
        WorkManager.getInstance(context.actual).cancelUniqueWork(tag)
    }

    companion object {
        private const val tag = "AppRefresh"
    }
}
