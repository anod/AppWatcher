package com.anod.appwatcher.backup

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.gdrive.ApiClientAsyncTask
import com.anod.appwatcher.backup.gdrive.SyncConnectedWorker
import com.anod.appwatcher.backup.gdrive.SyncTask
import com.anod.appwatcher.utils.GooglePlayServices
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive

/**
 * @author alex
 * *
 * @date 1/19/14
 */
class GDriveSync : GooglePlayServices, SyncTask.Listener {

    var listener: Listener? = null

    fun showResolutionNotification(resolution: PendingIntent) {
        val builder = NotificationCompat.Builder(mContext)
        builder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_update)
                .setContentTitle("Google Drive sync failed.")
                .setContentText("Required user action")
                .setContentIntent(resolution)

        val notification = builder.build()
        val mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, notification)
    }

    interface Listener {
        fun onGDriveConnect()
        fun onGDriveSyncProgress()
        fun onGDriveSyncStart()
        fun onGDriveSyncFinish()
        fun onGDriveError()
    }

    constructor(activity: Activity) : super(activity)

    constructor(context: Context) : super(context)

    override fun onConnectAction(action: Int) {
        listener?.onGDriveConnect()
        if (action == ACTION_SYNC) {
            SyncTask(mContext, this, createGoogleApiClientBuilder().build()).execute()
        }
    }

    override fun onConnectionError() {
        listener?.onGDriveError()
    }

    fun sync() {
        listener?.onGDriveSyncStart()
        if (!isConnected) {
            connectWithAction(ACTION_SYNC)
        } else {
            SyncTask(mContext, this, createGoogleApiClientBuilder().build()).execute()
        }
    }

    @Throws(Exception::class)
    fun syncLocked() {
        if (!isConnected) {
            connectLocked()
        }
        val worker = SyncConnectedWorker(mContext, mGoogleApiClient!!)
        worker.doSyncInBackground()
    }

    override fun createGoogleApiClientBuilder(): GoogleApiClient.Builder {
        return GoogleApiClient.Builder(mContext)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
    }

    override fun onResult(result: ApiClientAsyncTask.Result) {
        if (result.status) {
            listener?.onGDriveSyncFinish()
        } else {
            Toast.makeText(mContext, result.ex?.message ?: "Error", Toast.LENGTH_SHORT).show()
            listener?.onGDriveError()
        }
    }

    companion object {
        private val ACTION_SYNC = 2
        private val NOTIFICATION_ID = 2
    }
}