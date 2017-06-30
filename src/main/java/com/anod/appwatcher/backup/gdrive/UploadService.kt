package com.anod.appwatcher.backup.gdrive

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import com.anod.appwatcher.App
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.backup.GDriveSync
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.utils.GooglePlayServices
import com.google.android.gms.gcm.*
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * @date 13/06/2017
 */
class UploadService : GcmTaskService() {

    companion object {
        private const val windowStartDelaySeconds = 30L
        private const val windowEndDelaySeconds = 300L

        private val TASK_TAG = "GDriveUpload"

        fun schedule(context: Context) {
            val task = OneoffTask.Builder()
                    .setExtras(Bundle())
                    .setService(UploadService::class.java)
                    .setTag(TASK_TAG)
                    .setRequiresCharging(false)
                    .setPersisted(true)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setUpdateCurrent(true)
                    .setExecutionWindow(windowStartDelaySeconds,windowEndDelaySeconds)
                    .build()

            App.provide(context).gcmNetworkManager.schedule(task)
        }
    }

    override fun onRunTask(taskParams: TaskParams): Int {
        AppLog.d("Scheduled call executed. Task: " + taskParams.tag)

        AppLog.d("DriveSync perform upload")
        val driveSync = GDriveSync(applicationContext)
        val prefs = App.provide(applicationContext).prefs
        try {
            driveSync.uploadLocked()
            prefs.lastDriveSyncTime = System.currentTimeMillis()
        } catch (e: GooglePlayServices.ResolutionException) {
            driveSync.showResolutionNotification(e.resolution)
            AppLog.e(e)
        } catch (e: Exception) {
            AppLog.e(e)
        }

        return GcmNetworkManager.RESULT_SUCCESS
    }
}