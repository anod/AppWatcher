package com.anod.appwatcher.backup.gdrive

import android.content.Context
import android.os.Bundle
import com.anod.appwatcher.App
import com.firebase.jobdispatcher.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.android.log.AppLog
import info.anodsplace.appwatcher.framework.BackgroundTask

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

    private var runner: BackgroundTask<GoogleSignInAccount, Boolean>? = null

    override fun onStartJob(job: JobParameters?): Boolean {
        if (job == null) return false

        AppLog.d("Scheduled call executed. Task: " + job.tag)
        AppLog.d("DriveSync perform upload")

        val googleAccount = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (googleAccount == null) {
            AppLog.e("Account is null")
            return false
        }

        this.runner = BackgroundTask(object : BackgroundTask.Worker<GoogleSignInAccount, Boolean>(googleAccount) {
            override fun run(param: GoogleSignInAccount): Boolean {

                val worker = UploadConnectedWorker(applicationContext, googleAccount)
                try {
                    worker.doUploadInBackground()
                    return true
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