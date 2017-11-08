package com.anod.appwatcher.sync

import android.content.Intent
import android.os.Bundle
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.framework.BackgroundTask
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import info.anodsplace.android.log.AppLog

class SyncTaskService : JobService() {
    var runner: BackgroundTask<Void?, Int>? = null

    override fun onStartJob(job: JobParameters?): Boolean {
        if (job == null) return false

        AppLog.d("Scheduled call executed. Task: " + job.tag)

        this.runner = BackgroundTask(object : BackgroundTask.Worker<Void?, Int>(null) {
            override fun run(param: Void?): Int {
                val syncAdapter = VersionsCheck(applicationContext)
                val contentProviderClient = contentResolver.acquireContentProviderClient(DbContentProvider.authority)

                val extras = job.extras ?: Bundle.EMPTY
                return syncAdapter.perform(extras, contentProviderClient)
            }

            override fun finished(result: Int) {
                val finishIntent = Intent(VersionsCheck.SYNC_STOP)
                finishIntent.putExtra(VersionsCheck.EXTRA_UPDATES_COUNT, result)
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



