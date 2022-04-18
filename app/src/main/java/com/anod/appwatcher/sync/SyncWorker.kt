package com.anod.appwatcher.sync

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import info.anodsplace.applog.AppLog
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {

        AppLog.d("Scheduled call executed. Id: $id")

        val syncAdapter = get<UpdateCheck>()
        val result = syncAdapter.perform(inputData)

        val finishIntent = Intent(UpdateCheck.syncStop)
        finishIntent.putExtra(UpdateCheck.extrasUpdatesCount, result)
        applicationContext.sendBroadcast(finishIntent)

        return Result.success()
    }
}