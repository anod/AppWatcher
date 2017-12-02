package com.anod.appwatcher.model

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.support.v4.util.SimpleArrayMap

import finsky.api.model.Document

class AddWatchAppAsyncTask(
        context: Context,
        private val appList: WatchAppList,
        private val listener: AddWatchAppAsyncTask.Listener) : AsyncTask<Document, Void, SimpleArrayMap<String, Int>>() {

    @SuppressLint("StaticFieldLeak")
    private val context = context.applicationContext

    interface Listener {
        fun onAddAppTaskFinish(result: SimpleArrayMap<String, Int>)
    }

    override fun doInBackground(vararg documents: Document): SimpleArrayMap<String, Int> {
        appList.attach(context)
        val result = SimpleArrayMap<String, Int>()
        for (doc in documents) {
            if (isCancelled) {
                return result
            }
            val info = AppInfo(doc)
            val status = appList.addSync(info)
            result.put(info.packageName, status)
        }
        appList.detach()
        return result
    }

    override fun onPostExecute(result: SimpleArrayMap<String, Int>) {
        listener.onAddAppTaskFinish(result)
    }
}