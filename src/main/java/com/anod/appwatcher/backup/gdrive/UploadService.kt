package com.anod.appwatcher.backup.gdrive

import android.content.Context
import android.os.Bundle
import com.anod.appwatcher.App
import com.anod.appwatcher.backup.GDriveSync
import com.anod.appwatcher.framework.BackgroundTask
import com.anod.appwatcher.framework.GooglePlayServices
import com.firebase.jobdispatcher.*
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * @date 13/06/2017
 */
class UploadService : JobService() {

    companion object {
        private const val windowStartDelaySeconds = 30
        private const val windowEndDelaySeconds = 300

        private const val tag = "GDriveUpload"

        fun schedule(context: Context) {
            val dispatcher = App.provide(context).jobDispatcher

            val task = dispatcher.newJobBuilder()
                    .setService(UploadService::class.java)
                    .setTag(tag)
                    .setRecurring(false)
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    .setTrigger(Trigger.executionWindow(windowStartDelaySeconds, windowEndDelaySeconds))
                    .setReplaceCurrent(true)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .setExtras(Bundle())
                    .build()

            dispatcher.mustSchedule(task)
        }
    }

    private var runner: BackgroundTask<Void?, Boolean>? = null

    override fun onStartJob(job: JobParameters?): Boolean {
        if (job == null) return false

        AppLog.d("Scheduled call executed. Task: " + job.tag)
        AppLog.d("DriveSync perform upload")

        this.runner = BackgroundTask(object : BackgroundTask.Worker<Void?, Boolean>(null) {
            override fun run(param: Void?): Boolean {

                val driveSync = GDriveSync(applicationContext)
                try {
                    driveSync.uploadLocked()
                    return true
                } catch (e: GooglePlayServices.ResolutionException) {
                    driveSync.showResolutionNotification(e.resolution)
                    AppLog.e(e)
                    return false
                } catch (e: Exception) {
                    AppLog.e(e)
                    return false
                }
            }

            override fun finished(result: Boolean) {
                if (result) {
                    val prefs = App.provide(applicationContext).prefs
                    prefs.lastDriveSyncTime = System.currentTimeMillis()
                }
                jobFinished(job, !result)
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