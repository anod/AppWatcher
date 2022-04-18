package com.anod.appwatcher.sync

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.*
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/**
 * @author Alex Gavrishev
 * *
 * @date 27/05/2016.
 */

class SyncScheduler(private val context: ApplicationContext) {
    private val wm: WorkManager
        get() = WorkManager.getInstance(context.actual)

    constructor(context: Context) : this(ApplicationContext(context))

    fun schedule(requiresCharging: Boolean, requiresWifi: Boolean, windowStartSec: Long, replace: Boolean): Flow<Operation.State> {
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

        val policy = if (replace)
            ExistingPeriodicWorkPolicy.REPLACE
        else
            ExistingPeriodicWorkPolicy.KEEP
        AppLog.i("Schedule sync in ${windowStartSec / 3600} hours (${if (replace) "Replace" else "Keep existing"})", "PeriodicWork")
        return wm.enqueueUniquePeriodicWork(tag, policy, request)
            .state
            .map {
                when (it) {
                    is Operation.State.SUCCESS -> AppLog.i("Sync scheduled", "PeriodicWork")
                    is Operation.State.IN_PROGRESS -> AppLog.i("Sync schedule in progress", "PeriodicWork")
                    is Operation.State.FAILURE -> AppLog.e("Sync schedule error", "PeriodicWork", it.throwable)
                }
                it
            }.asFlow()
    }

    fun execute(): Flow<Operation.State> {
        val constraints: Constraints = Constraints.Builder().apply {
            setRequiresCharging(false)
            setRequiredNetworkType(NetworkType.CONNECTED)
            setRequiresDeviceIdle(false)
        }.build()

        val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(
                Data.Builder()
                    .putBoolean(UpdateCheck.extrasManual, true)
                    .build()
            )
                .setConstraints(constraints)
                .build()

        AppLog.i("Enqueue update check", "OneTimeWork")
        return wm.enqueueUniqueWork(tagManual, ExistingWorkPolicy.REPLACE, request)
            .state
            .map {
                when (it) {
                    is Operation.State.SUCCESS -> AppLog.i("Update scheduled", "OneTimeWork")
                    is Operation.State.IN_PROGRESS -> AppLog.i("Update schedule in progress", "OneTimeWork")
                    is Operation.State.FAILURE -> AppLog.e("Update schedule error", "OneTimeWork", it.throwable)
                }
                it
            }.asFlow()
    }

    fun cancel(): Flow<Operation.State> {
        AppLog.i("Cancel scheduled sync", "SyncSchedule")
        return wm.cancelUniqueWork(tag)
            .state
            .map {
                when (it) {
                    is Operation.State.SUCCESS -> AppLog.i("Sync canceled", "PeriodicWork")
                    is Operation.State.IN_PROGRESS -> AppLog.i("Sync cancel in progress", "PeriodicWork")
                    is Operation.State.FAILURE -> AppLog.e("Sync cancel error", "PeriodicWork", it.throwable)
                }
                it
            }.asFlow()
    }

    companion object {
        private const val tag = "AppRefresh"
        private const val tagManual = "AppRefreshManual"
    }
}