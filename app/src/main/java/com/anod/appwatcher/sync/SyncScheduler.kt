package com.anod.appwatcher.sync

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.*
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

    fun schedule(requiresCharging: Boolean, requiresWifi: Boolean, windowStartSec: Long): LiveData<Operation.State> {
        val constraints: Constraints = Constraints.Builder().apply {
            setRequiresCharging(requiresCharging)
            if (requiresWifi) {
                setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }
        }.build()

        val request: PeriodicWorkRequest =
                PeriodicWorkRequest.Builder(SyncWorker::class.java, windowStartSec, TimeUnit.SECONDS, PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS)
                        .setInputData(Data.EMPTY)
                        .setConstraints(constraints)
                        .build()

        AppLog.i("Schedule sync in ${windowStartSec / 3600} hours")
        return WorkManager
                .getInstance(context.actual)
                .enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, request)
                .state
                .map {
                    when (it) {
                        is Operation.State.SUCCESS -> AppLog.i("Operation scheduled")
                        is Operation.State.IN_PROGRESS -> AppLog.i("Operation schedule in progress")
                        is Operation.State.FAILURE -> AppLog.e("Operation schedule error", it.throwable)
                    }
                    it
                }
    }

    fun execute(): LiveData<Operation.State> {
        val constraints: Constraints = Constraints.Builder().apply {
            setRequiresCharging(false)
            setRequiredNetworkType(NetworkType.CONNECTED)
            if (Build.VERSION.SDK_INT >= 23) setRequiresDeviceIdle(false)
        }.build()

        val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                .setInputData(Data.Builder()
                        .putBoolean(UpdateCheck.extrasManual, !BuildConfig.DEBUG)
                        .build())
                .setConstraints(constraints)
                .build()

        AppLog.i("Enqueue update check")
        return WorkManager
                .getInstance(context.actual)
                .enqueue(request)
                .state
                .map {
                    when (it) {
                        is Operation.State.SUCCESS -> AppLog.i("Operation scheduled")
                        is Operation.State.IN_PROGRESS -> AppLog.i("Operation schedule in progress")
                        is Operation.State.FAILURE -> AppLog.e("Operation schedule error", it.throwable)
                    }
                    it
                }
    }

    fun cancel(): LiveData<Operation.State> {
        AppLog.i("Cancel scheduled sync")
        return WorkManager
                .getInstance(context.actual)
                .cancelUniqueWork(tag)
                .state
                .map {
                    when (it) {
                        is Operation.State.SUCCESS -> AppLog.i("Operation canceled")
                        is Operation.State.IN_PROGRESS -> AppLog.i("Operation cancel in progress")
                        is Operation.State.FAILURE -> AppLog.e("Operation cancel error", it.throwable)
                    }
                    it
                }
    }

    companion object {
        private const val tag = "AppRefresh"
    }
}
