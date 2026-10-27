package com.anod.appwatcher.sync

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.anod.appwatcher.diagnostics.DiagnosticDecision
import com.anod.appwatcher.diagnostics.DiagnosticEvent
import com.anod.appwatcher.diagnostics.DiagnosticRecord
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import info.anodsplace.applog.AppLog
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * @author Alex Gavrishev
 * *
 * @date 27/05/2016.
 */

class SyncScheduler(
    private val context: info.anodsplace.context.ApplicationContext,
    private val diagnostics: DiagnosticRecorder
) {
    private val wm: WorkManager
        get() = WorkManager.getInstance(context.actual)

    constructor(context: Context, diagnostics: DiagnosticRecorder) : this(info.anodsplace.context.ApplicationContext(context), diagnostics)

    fun schedule(
        requiresCharging: Boolean,
        requiresWifi: Boolean,
        windowStartSec: Long,
        update: Boolean
    ): Flow<Operation.State> {
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

        val policy = if (update) {
            ExistingPeriodicWorkPolicy.UPDATE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }
        AppLog.i("Schedule sync in ${windowStartSec / 3600} hours (${if (update) "Update" else "Keep existing"})", "PeriodicWork")
        diagnostics.record(DiagnosticRecord(
            DiagnosticEvent.SCHEDULE, run = request.id,
            decision = if (update) DiagnosticDecision.PERIODIC_UPDATE else DiagnosticDecision.PERIODIC_KEEP,
            count = windowStartSec, flag = requiresCharging, secondaryFlag = requiresWifi
        ))
        return wm.enqueueUniquePeriodicWork(TAG, policy, request)
            .state
            .asFlow()
            .onEach {
                recordOperation(it, request.id)
                when (it) {
                    is Operation.State.SUCCESS -> AppLog.i("Sync scheduled", "PeriodicWork")
                    is Operation.State.IN_PROGRESS -> AppLog.i("Sync schedule in progress", "PeriodicWork")
                    is Operation.State.FAILURE -> AppLog.e("Sync schedule error", "PeriodicWork", it.throwable)
                }
            }
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
                    .putBoolean(UpdateCheck.EXTRAS_MANUAL, true)
                    .build()
            )
            .setConstraints(constraints)
            .build()

        AppLog.i("Enqueue update check", "OneTimeWork")
        diagnostics.record(DiagnosticRecord(
            DiagnosticEvent.SCHEDULE, run = request.id, decision = DiagnosticDecision.MANUAL
        ))
        return wm.enqueueUniqueWork(TAG_MANUAL, ExistingWorkPolicy.REPLACE, request)
            .state
            .asFlow()
            .onEach {
                recordOperation(it, request.id)
                when (it) {
                    is Operation.State.SUCCESS -> AppLog.i("Update scheduled", "OneTimeWork")
                    is Operation.State.IN_PROGRESS -> AppLog.i("Update schedule in progress", "OneTimeWork")
                    is Operation.State.FAILURE -> AppLog.e("Update schedule error", "OneTimeWork", it.throwable)
                }
            }
    }

    fun cancel(): Flow<Operation.State> {
        AppLog.i("Cancel scheduled sync", "SyncSchedule")
        diagnostics.record(DiagnosticRecord(DiagnosticEvent.CANCEL_SCHEDULE))
        return wm.cancelUniqueWork(TAG)
            .state
            .map {
                recordOperation(it, null)
                when (it) {
                    is Operation.State.SUCCESS -> AppLog.i("Sync canceled", "PeriodicWork")
                    is Operation.State.IN_PROGRESS -> AppLog.i("Sync cancel in progress", "PeriodicWork")
                    is Operation.State.FAILURE -> AppLog.e("Sync cancel error", "PeriodicWork", it.throwable)
                }
                it
            }.asFlow()
    }

    private fun recordOperation(state: Operation.State, id: java.util.UUID?) {
        diagnostics.record(DiagnosticRecord(
            DiagnosticEvent.SCHEDULE_RESULT, run = id,
            decision = when (state) {
                is Operation.State.SUCCESS -> DiagnosticDecision.SUCCESS
                is Operation.State.FAILURE -> DiagnosticDecision.FAILED
                else -> DiagnosticDecision.IN_PROGRESS
            }
        ))
    }

    companion object {
        internal const val TAG = "AppRefresh"
        internal const val TAG_MANUAL = "AppRefreshManual"
    }
}