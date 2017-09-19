package com.anod.appwatcher.sync

import android.content.Intent
import android.os.Bundle
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.utils.BackgroundTask
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import info.anodsplace.android.log.AppLog

class SyncTaskService : JobService() {
    var runner: BackgroundTask.AsyncTaskRunner<Void?, Int>? = null

    override fun onStartJob(job: JobParameters?): Boolean {
        if (job == null) return false

        AppLog.d("Scheduled call executed. Task: " + job.tag)

        this.runner = BackgroundTask.execute(object : BackgroundTask.Worker<Void?, Int>(null) {
            override fun run(param: Void?): Int {
                val syncAdapter = SyncAdapter(applicationContext)
                val contentProviderClient = contentResolver.acquireContentProviderClient(DbContentProvider.authority)

                val extras = job.extras ?: Bundle.EMPTY
                return syncAdapter.onPerformSync(extras, contentProviderClient)
            }

            override fun finished(result: Int) {
                val finishIntent = Intent(SyncAdapter.SYNC_STOP)
                finishIntent.putExtra(SyncAdapter.EXTRA_UPDATES_COUNT, result)
                sendBroadcast(finishIntent)

                jobFinished(job, false)
            }
        })
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        AppLog.e("Job stopped. Task: ${job?.tag ?: "unknown"}")
        this.runner?.cancel(true)
        return true
    }
}



