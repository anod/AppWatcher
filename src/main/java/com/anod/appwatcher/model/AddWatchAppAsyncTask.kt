package com.anod.appwatcher.model

import android.content.Context
import android.os.AsyncTask
import android.support.v4.util.SimpleArrayMap

import com.google.android.finsky.api.model.Document

class AddWatchAppAsyncTask(
        private val mNewAppHandler: WatchAppList,
        private val mContext: Context,
        private val mListener: AddWatchAppAsyncTask.Listener) : AsyncTask<Document, Void, SimpleArrayMap<String, Int>>() {

    interface Listener {
        fun onAddAppTaskFinish(result: SimpleArrayMap<String, Int>)
    }

    override fun doInBackground(vararg documents: Document): SimpleArrayMap<String, Int> {
        mNewAppHandler.attach(mContext)
        val result = SimpleArrayMap<String, Int>()
        for (doc in documents) {
            if (isCancelled) {
                return result
            }
            val info = AppInfo(doc)
            val status = mNewAppHandler.addSync(info)
            result.put(info.packageName, status)
        }
        mNewAppHandler.detach()
        return result
    }

    override fun onPostExecute(result: SimpleArrayMap<String, Int>) {
        mListener.onAddAppTaskFinish(result)
    }
}