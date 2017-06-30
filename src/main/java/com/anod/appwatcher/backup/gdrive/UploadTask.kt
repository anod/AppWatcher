package com.anod.appwatcher.backup.gdrive

import android.content.Context

import com.google.android.gms.common.api.GoogleApiClient

import info.anodsplace.android.log.AppLog

/**
 * @author alex
 * *
 * @date 7/29/14
 */
class UploadTask(private val context: Context, private val listener: UploadTask.Listener, client: GoogleApiClient)
    : ApiClientAsyncTask(client) {

    interface Listener {
        fun onUploadTaskResult(result: Result)
    }

    override fun doInBackgroundConnected(): Result {
        val worker = UploadConnectedWorker(context, googleApiClient)
        try {
            worker.doUploadInBackground()
        } catch (e: Exception) {
            AppLog.e(e)
            return Result(false, e)
        }

        return Result(true, null)
    }

    override fun onPostExecute(result: Result) {
        listener.onUploadTaskResult(result)
    }
}
