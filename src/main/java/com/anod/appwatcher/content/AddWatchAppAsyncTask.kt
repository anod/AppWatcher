package com.anod.appwatcher.content

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.model.AppInfo

import finsky.api.model.Document
import info.anodsplace.framework.app.ApplicationContext


class AddWatchAppAsyncTask(
        context: ApplicationContext,
        private val appList: WatchAppList,
        private val listener: Listener) : AsyncTask<Document, Void, SimpleArrayMap<String, Int>>() {

    @SuppressLint("StaticFieldLeak")
    private val context = context.actual

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
        context.sendBroadcast(Intent(listChanged))
    }

    companion object {
        const val listChanged = "com.anod.appwatcher.list.changed"
    }
}