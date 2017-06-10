package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Context
import android.os.AsyncTask
import android.support.v4.util.SimpleArrayMap
import com.android.volley.VolleyError
import com.anod.appwatcher.market.BulkDetailsEndpoint
import com.anod.appwatcher.market.PlayStoreEndpoint
import com.anod.appwatcher.model.AddWatchAppAsyncTask
import com.anod.appwatcher.model.WatchAppList
import com.google.android.finsky.api.model.Document
import java.util.*

internal class ImportBulkManager(
        private val mContext: Context,
        private val mListener: ImportBulkManager.Listener)
    : PlayStoreEndpoint.Listener, AddWatchAppAsyncTask.Listener {

    private val mEndpoint = BulkDetailsEndpoint(mContext)
    private val mWatchAppList = WatchAppList(null)
    private var listsDocIds: MutableList<MutableList<String>> = ArrayList()
    private var currentBulk: Int = 0
    private var mTask: AsyncTask<Document, Void, SimpleArrayMap<String, Int>>? = null

    interface Listener {
        fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>)
        fun onImportFinish()
        fun onImportStart(docIds: List<String>)
    }

    init {
        mEndpoint.listener = this
    }

    fun init() {
        listsDocIds = ArrayList()
        currentBulk = 0
    }

    fun stop() {
        mEndpoint.reset()
        if (mTask != null && !mTask!!.isCancelled) {
            mTask!!.cancel(true)
            mTask = null
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
        mListener.onImportStart(docIds)
        mEndpoint.setDocIds(docIds)
        mEndpoint.startAsync()
    }

    override fun onDataChanged() {
        val docs = mEndpoint.documents
        mTask = AddWatchAppAsyncTask(mWatchAppList, mContext, this).execute(*docs.toTypedArray())
    }

    override fun onErrorResponse(error: VolleyError) {
        val docIds = listsDocIds[currentBulk]
        mListener.onImportProgress(docIds, SimpleArrayMap<String, Int>())
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            mListener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    fun setAccount(account: Account, authSubToken: String) {
        mEndpoint.setAccount(account, authSubToken)
    }

    override fun onAddAppTaskFinish(result: SimpleArrayMap<String, Int>) {
        val docIds = listsDocIds[currentBulk]
        mListener.onImportProgress(docIds, result)
        currentBulk++
        if (currentBulk == listsDocIds.size) {
            mListener.onImportFinish()
        } else {
            nextBulk()
        }
    }

    companion object {
        private val BULK_SIZE = 20
    }
}