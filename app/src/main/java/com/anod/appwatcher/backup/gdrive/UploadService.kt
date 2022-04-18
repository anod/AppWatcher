package com.anod.appwatcher.backup.gdrive

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.anod.appwatcher.Application
import com.anod.appwatcher.SettingsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import java.util.concurrent.TimeUnit

/**
 * @author Alex Gavrishev
 * @date 13/06/2017
 */
class UploadService(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        private const val windowStartDelaySeconds = 60L
        private const val tag = "GDriveUpload"

        fun schedule(requiresWifi: Boolean, requiresCharging: Boolean, context: Context) {

            val constraints: Constraints = Constraints.Builder().apply {
                setRequiresCharging(requiresCharging)
                if (requiresWifi) {
                    setRequiredNetworkType(NetworkType.UNMETERED)
                } else {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }
            }.build()

            val request: OneTimeWorkRequest =
                    OneTimeWorkRequest.Builder(UploadService::class.java)
                            .setInputData(Data.EMPTY)
                            .setConstraints(constraints)
                            .setInitialDelay(windowStartDelaySeconds, TimeUnit.SECONDS)
                            .build()

            WorkManager.getInstance(context).enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
        }
    }

    override suspend fun doWork(): Result {
        AppLog.d("Scheduled call executed. Id: $id")
        AppLog.d("DriveSync perform upload")

        val googleAccount = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (googleAccount == null) {
            AppLog.e("Account is null")
            return Result.failure()
        }

        val worker = GDriveUpload(applicationContext, googleAccount)
        try {
            worker.doUploadInBackground()
        } catch (e: Exception) {
            AppLog.e("UploadService::doWork - ${e.message}", e)
            DriveService.extractUserRecoverableException(e)?.let {
                val settingActivity = Intent(applicationContext, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                GDriveSignIn.showResolutionNotification(
                        PendingIntent.getActivity(applicationContext, 0, settingActivity, 0), ApplicationContext(applicationContext)
                )
            }
            return Result.failure()
        }

        val prefs = Application.provide(applicationContext).prefs
        prefs.lastDriveSyncTime = System.currentTimeMillis()
        return Result.success()
    }
}