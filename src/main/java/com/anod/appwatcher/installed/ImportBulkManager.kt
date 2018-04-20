package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Context
import android.os.AsyncTask
import android.support.v4.util.SimpleArrayMap
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.model.AddWatchAppAsyncTask
import com.anod.appwatcher.model.WatchAppList
import finsky.api.BulkDocId
import finsky.api.model.DfeBulkDetails
import finsky.api.model.DfeModel
import finsky.api.model.Document
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint
import java.util.*

internal class ImportBulkManager(
        private val context: Context,
        private val listener: ImportBulkManager.Listener)
    : PlayStoreEndpoint.Listener, AddWatchAppAsyncTask.Listener {

    private val watchAppList = WatchAppList(null)
    private var listsDocIds: MutableList<MutableList<BulkDocId>> = ArrayList()
    private var currentBulk: Int = 0
    private var asyncTask: AsyncTask<Document, Void, SimpleArrayMap<String, Int>>? = null

    private var account: Account? = null
    private var authSubToken: String = ""

    interface Listener {
        fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>)
        fun onImportFinish()
        fun onImportStart(docIds: List<String>)
    }

    fun init() {
        listsDocIds = ArrayList()
        currentBulk = 0
    }

    fun stop() {
//        endpoint.reset()
        if (asyncTask != null && !asyncTask!!.isCancelled) {
            asyncTask!!.cancel(true)
            asyncTask = null
        }
    }

    fun addPackage(packageName: String, versionNumber: Int) {
        var currentList: MutableList<BulkDocId>? = null
        if (listsDocIds.size > currentBulk) {
            currentList = listsDocIds[currentBulk]
        }
        if (currentList == null) {
            currentList = ArrayList()
            listsDocIds.add(currentList)
        } else if (currentList.size > BULK_SIZE) {
            currentBulk++
            currentList = ArrayList()
            listsDocIds.add(currentList)
        }
        currentList.add(BulkDocId(packageName, versionNumber))
    }

    val isEmpty: Boolean
        get() = listsDocIds.isEmpty()

    fun start(account: Account, authSubToken: String) {
        this.account = account
        this.authSubToken = authSubToken
        currentBulk = 0
        nextBulk()
    }

    private fun nextBulk() {
        val account = this.account ?: return

        val docIds = listsDocIds[currentBulk]
        listener.onImportStart(docIds.map { it.packageName })
        val endpoint = BulkDetailsEndpoint(context, App.provide(context).requestQueue, App.provide(context).deviceInfo, account, docIds)
        endpoint.listener = this
        endpoint.authToken = authSubToken
        endpoint.startAsync()
    }

    override fun onDataChanged(data: DfeModel) {
        val docs = (data as DfeBulkDetails).documents.toTypedArray()
        asyncTask = AddWatchAppAsyncTask(ApplicationContext(context), watchAppList, this).execute(*docs)
    }

    override fun onErrorResponse(error: VolleyError) {
        val docIds = listsDocIds[currentBulk]
        listener.onImportProgress(docIds.map { it.packageName }, SimpleArrayMap())
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            listener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    override fun onAddAppTaskFinish(result: SimpleArrayMap<String, Int>) {
        val docIds = listsDocIds[currentBulk]
        listener.onImportProgress(docIds.map { it.packageName }, result)
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            listener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    companion object {
        private const val BULK_SIZE = 100
    }
}