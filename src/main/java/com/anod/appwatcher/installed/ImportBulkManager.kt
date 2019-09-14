// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Context
import androidx.collection.SimpleArrayMap
import com.android.volley.VolleyError
import com.anod.appwatcher.Application
import finsky.api.BulkDocId
import finsky.api.model.DfeBulkDetails
import finsky.api.model.DfeModel
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.*

internal class ImportBulkManager(
        private val context: Context,
        private val listener: Listener)
    : PlayStoreEndpoint.Listener {

    private var listsDocIds: MutableList<MutableList<BulkDocId>?> = mutableListOf()
    private var currentBulk: Int = 0

    private var account: Account? = null
    private var authSubToken: String = ""
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    interface Listener {
        fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>)
        fun onImportFinish()
        fun onImportStart(docIds: List<String>)
    }

    fun init() {
        listsDocIds = mutableListOf()
        currentBulk = 0
    }

    fun stop() {
        job.cancel()
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

        val docIds = listsDocIds[currentBulk] ?: emptyList<BulkDocId>()
        if (docIds.isEmpty()) {
            listener.onImportFinish()
            return
        }
        listener.onImportStart(docIds.map { it.packageName })
        val endpoint = BulkDetailsEndpoint(context, Application.provide(context).requestQueue, Application.provide(context).deviceInfo, account, docIds)
        endpoint.listener = this
        endpoint.authToken = authSubToken
        endpoint.startAsync()
    }

    override fun onDataChanged(data: DfeModel) {
        val docs = (data as DfeBulkDetails).documents.toTypedArray()
        val task = ImportTask(ApplicationContext(context))
        coroutineScope.launch {
            val result = task.execute(*docs)
            onAddAppTaskFinish(result)
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        val docIds = listsDocIds[currentBulk] ?: emptyList<BulkDocId>()
        listener.onImportProgress(docIds.map { it.packageName }, SimpleArrayMap())
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            listener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    private suspend fun onAddAppTaskFinish(result: SimpleArrayMap<String, Int>) = withContext(Main) {
        val docIds = listsDocIds[currentBulk] ?: emptyList<BulkDocId>()
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