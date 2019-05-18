package com.anod.appwatcher.sync

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {

        AppLog.d("Scheduled call executed. Id: $id")

        val syncAdapter = UpdateCheck(ApplicationContext(applicationContext))
        val result  = syncAdapter.perform(inputData)

        val finishIntent = Intent(UpdateCheck.syncStop)
        finishIntent.putExtra(UpdateCheck.extrasUpdatesCount, result)
        applicationContext.sendBroadcast(finishIntent)

        return Result.success()
    }
}



