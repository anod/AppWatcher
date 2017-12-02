package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Context
import android.os.AsyncTask
import android.support.v4.util.SimpleArrayMap
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.model.AddWatchAppAsyncTask
import com.anod.appwatcher.model.WatchAppList
import finsky.api.model.Document
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint
import java.util.*

internal class ImportBulkManager(
        private val context: Context,
        private val listener: ImportBulkManager.Listener)
    : PlayStoreEndpoint.Listener, AddWatchAppAsyncTask.Listener {

    private val endpoint = BulkDetailsEndpoint(context, App.provide(context).requestQueue, App.provide(context).deviceInfo)
    private val watchAppList = WatchAppList(null)
    private var listsDocIds: MutableList<MutableList<String>> = ArrayList()
    private var currentBulk: Int = 0
    private var asyncTask: AsyncTask<Document, Void, SimpleArrayMap<String, Int>>? = null

    interface Listener {
        fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>)
        fun onImportFinish()
        fun onImportStart(docIds: List<String>)
    }

    init {
        endpoint.listener = this
    }

    fun init() {
        listsDocIds = ArrayList()
        currentBulk = 0
    }

    fun stop() {
        endpoint.reset()
        if (asyncTask != null && !asyncTask!!.isCancelled) {
            asyncTask!!.cancel(true)
            asyncTask = null
        }
    }

    fun addPackage(packageName: String) {
        var currentList: MutableList<String>? = null
        if (listsDocIds.size > currentBulk) {
            currentList = listsDocIds[currentBulk]
        }
        if (currentList == null) {
            currentList = ArrayList<String>()
            listsDocIds.add(currentList)
        } else if (currentList.size > BULK_SIZE) {
            currentBulk++
            currentList = ArrayList<String>()
            listsDocIds.add(currentList)
        }
        currentList.add(packageName)
    }

    val isEmpty: Boolean
        get() = listsDocIds.isEmpty()

    fun start() {
        currentBulk = 0
        nextBulk()
    }

    private fun nextBulk() {
        val docIds = listsDocIds[currentBulk]
        listener.onImportStart(docIds)
        endpoint.docIds = docIds
        endpoint.startAsync()
    }

    override fun onDataChanged() {
        val docs = endpoint.documents
        asyncTask = AddWatchAppAsyncTask(context, watchAppList, this).execute(*docs.toTypedArray())
    }

    override fun onErrorResponse(error: VolleyError) {
        val docIds = listsDocIds[currentBulk]
        listener.onImportProgress(docIds, SimpleArrayMap<String, Int>())
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            listener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    fun setAccount(account: Account, authSubToken: String) {
        endpoint.setAccount(account, authSubToken)
    }

    override fun onAddAppTaskFinish(result: SimpleArrayMap<String, Int>) {
        val docIds = listsDocIds[currentBulk]
        listener.onImportProgress(docIds, result)
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            listener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    companion object {
        private const val BULK_SIZE = 20
    }
}