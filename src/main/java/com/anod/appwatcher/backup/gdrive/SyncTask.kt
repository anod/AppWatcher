package com.anod.appwatcher.backup.gdrive

import android.content.Context

import com.google.android.gms.common.api.GoogleApiClient

import info.anodsplace.android.log.AppLog

/**
 * @author alex
 * *
 * @date 7/29/14
 */
class SyncTask(private val context: Context, private val mListener: SyncTask.Listener, client: GoogleApiClient)
    : ApiClientAsyncTask(client) {

    interface Listener {
        fun onResult(result: Result)
    }

    override fun doInBackgroundConnected(): Result {
        val worker = SyncConnectedWorker(context, googleApiClient)
        try {
            worker.doSyncInBackground()
        } catch (e: Exception) {
            AppLog.e(e)
            return Result(false, e)
        }

        return Result(true, null)
    }

    override fun onPostExecute(result: Result) {
        mListener.onResult(result)
    }
}
