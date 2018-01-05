package com.anod.appwatcher.sync

import android.content.Intent
import android.os.Bundle
import com.anod.appwatcher.App
import com.anod.appwatcher.content.DbContentProvider
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import info.anodsplace.android.log.AppLog
import info.anodsplace.appwatcher.framework.ApplicationContext
import info.anodsplace.appwatcher.framework.BackgroundTask

class SyncTaskService : JobService() {
    var runner: BackgroundTask<Void?, Int>? = null

    override fun onStartJob(job: JobParameters?): Boolean {
        if (job == null) return false

        AppLog.d("Scheduled call executed. Task: " + job.tag)

        this.runner = BackgroundTask(object : BackgroundTask.Worker<Void?, Int>(null) {
            override fun run(param: Void?): Int {
                val syncAdapter = UpdateCheck(ApplicationContext(applicationContext))
                val contentProviderClient = contentResolver.acquireContentProviderClient(DbContentProvider.authority)

                val extras = job.extras ?: Bundle.EMPTY
                return syncAdapter.perform(extras, contentProviderClient, App.log(applicationContext))
            }

            override fun finished(result: Int) {
                val finishIntent = Intent(UpdateCheck.syncStop)
                finishIntent.putExtra(UpdateCheck.extrasUpdatesCount, result)
                sendBroadcast(finishIntent)

                jobFinished(job, false)
            }
        })
        this.runner?.execute()
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        AppLog.e("Job stopped. Task: ${job?.tag ?: "unknown"}")
        this.runner?.cancel(true)
        return true
    }
}



