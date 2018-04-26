package com.anod.appwatcher.backup.gdrive

import android.content.Context
import android.widget.Toast
import com.anod.appwatcher.content.DbContentProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask


/**
 * @author alex
 * *
 * @date 1/19/14
 */
class GDrive(private val context: ApplicationContext, private val googleAccount: GoogleSignInAccount, private var listener: Listener? = null) {

    constructor(context: Context, googleAccount: GoogleSignInAccount, listener: Listener? = null)
        : this(ApplicationContext(context), googleAccount, listener)


    interface Listener {
        fun onGDriveSyncProgress()
        fun onGDriveSyncStart()
        fun onGDriveSyncFinish()
        fun onGDriveError()
    }

    fun sync() {
        listener?.onGDriveSyncStart()
        BackgroundTask(object : BackgroundTask.Worker<GoogleSignInAccount, ApiClientAsyncTask.Result>(googleAccount) {
            override fun run(param: GoogleSignInAccount): ApiClientAsyncTask.Result {
                val worker = SyncConnectedWorker(context, param)
                try {
                    worker.doSyncInBackground()
                } catch (e: Exception) {
                    AppLog.e(e)
                    return ApiClientAsyncTask.Result(false, e)
                }

                return ApiClientAsyncTask.Result(true, null)
            }

            override fun finished(result: ApiClientAsyncTask.Result) {
                if (result.status) {
                    context.contentResolver.notifyChange(DbContentProvider.appsUri, null)
                    listener?.onGDriveSyncFinish()
                } else {
                    Toast.makeText(context.actual, result.ex?.message ?: "Error", Toast.LENGTH_SHORT).show()
                    listener?.onGDriveError()
                }
            }
        }).execute()
    }


    fun upload() {
        listener?.onGDriveSyncStart()
        BackgroundTask(object : BackgroundTask.Worker<GoogleSignInAccount, ApiClientAsyncTask.Result>(googleAccount) {
            override fun run(param: GoogleSignInAccount): ApiClientAsyncTask.Result {
                val worker = UploadConnectedWorker(context, googleAccount)
                try {
                    worker.doUploadInBackground()
                } catch (e: Exception) {
                    AppLog.e(e)
                    return ApiClientAsyncTask.Result(false, e)
                }

                return ApiClientAsyncTask.Result(true, null)
            }

            override fun finished(result: ApiClientAsyncTask.Result) {
                if (result.status) {
                    listener?.onGDriveSyncFinish()
                } else {
                    Toast.makeText(context.actual, result.ex?.message ?: "Error", Toast.LENGTH_SHORT).show()
                    listener?.onGDriveError()
                }
            }
        }).execute()
    }

}