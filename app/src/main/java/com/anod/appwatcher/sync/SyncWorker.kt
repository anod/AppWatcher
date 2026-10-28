package com.anod.appwatcher.sync

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        AppLog.i("Scheduled call executed. Id: $id, attempt: $runAttemptCount, tags: $tags", "SyncWorker")

        val result = try {
            get<UpdateCheck>().perform(inputData, stopReason = ::describeStopReason)
        } catch (e: CancellationException) {
            AppLog.w("Sync work $id stopped: ${describeStopReason() ?: "unknown"}", "SyncWorker")
            throw e
        } catch (e: Exception) {
            AppLog.e("Sync work $id failed: ${describeStopReason() ?: "unknown"}; failureKind=unexpected", "SyncWorker", e)
            throw e
        }

        val finishIntent = Intent(UpdateCheck.SYNC_STOP).apply {
            `package` = applicationContext.packageName
        }
        finishIntent.putExtra(UpdateCheck.EXTRA_UPDATES_COUNT, result)
        applicationContext.sendBroadcast(finishIntent)

        AppLog.i("Sync work $id finished with $result", "SyncWorker")
        return Result.success()
    }

    private fun describeStopReason(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return null
        }
        val reason = stopReason
        val name = when (reason) {
            WorkInfo.STOP_REASON_NOT_STOPPED -> "not-stopped"
            WorkInfo.STOP_REASON_CANCELLED_BY_APP -> "cancelled-by-app"
            WorkInfo.STOP_REASON_PREEMPT -> "preempt"
            WorkInfo.STOP_REASON_TIMEOUT -> "timeout"
            WorkInfo.STOP_REASON_DEVICE_STATE -> "device-state"
            WorkInfo.STOP_REASON_CONSTRAINT_BATTERY_NOT_LOW -> "constraint-battery-not-low"
            WorkInfo.STOP_REASON_CONSTRAINT_CHARGING -> "constraint-charging"
            WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY -> "constraint-connectivity"
            WorkInfo.STOP_REASON_CONSTRAINT_DEVICE_IDLE -> "constraint-device-idle"
            WorkInfo.STOP_REASON_CONSTRAINT_STORAGE_NOT_LOW -> "constraint-storage-not-low"
            WorkInfo.STOP_REASON_QUOTA -> "quota"
            WorkInfo.STOP_REASON_BACKGROUND_RESTRICTION -> "background-restriction"
            WorkInfo.STOP_REASON_APP_STANDBY -> "app-standby"
            WorkInfo.STOP_REASON_USER -> "user"
            WorkInfo.STOP_REASON_SYSTEM_PROCESSING -> "system-processing"
            WorkInfo.STOP_REASON_ESTIMATED_APP_LAUNCH_TIME_CHANGED -> "estimated-launch-time-changed"
            else -> "unknown"
        }
        return "$name($reason)"
    }
}