package com.anod.appwatcher.market

import android.accounts.Account
import android.content.Context

import com.android.volley.Response
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.AppWatcherApplication
import com.google.android.finsky.api.DfeApi
import com.google.android.finsky.api.DfeApiContext
import com.google.android.finsky.api.DfeApiImpl
import com.google.android.finsky.api.model.DfeModel
import com.google.android.finsky.api.model.OnDataChangedListener
import com.google.android.finsky.config.ContentLevel

import info.anodsplace.android.log.AppLog

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
abstract class PlayStoreEndpointBase internal constructor(context: Context) : PlayStoreEndpoint, Response.ErrorListener, OnDataChangedListener {
    protected val mContext: Context = context.applicationContext

    final override var authSubToken: String = ""
        private set
    override var listener: PlayStoreEndpoint.Listener? = null

    var data: DfeModel? = null
        internal set
    var dfeApi: DfeApi? = null
        private set
    private var mAccount: Account? = null

    override fun setAccount(account: Account, authSubToken: String): PlayStoreEndpoint {
        mAccount = account
        this.authSubToken = authSubToken
        return this
    }

    override fun startAsync() {
        init()
        executeAsync()
    }

    override fun startSync() {
        init()
        executeSync()
    }

    protected abstract fun executeAsync()
    protected abstract fun executeSync()

    private fun init() {
        if (dfeApi == null) {
            val queue = App.provide(mContext).requestQueue
            val deviceId = App.provide(mContext).deviceId
            val dfeApiContext = DfeApiContext.create(mContext, mAccount, authSubToken, deviceId, ContentLevel.create(mContext).dfeValue)
            dfeApi = DfeApiImpl(queue, dfeApiContext)
        }
        if (data == null) {
            data = createDfeModel()
            data!!.addDataChangedListener(this)
            data!!.addErrorListener(this)
        }
    }

    override fun reset() {
        if (data != null) {
            data!!.unregisterAll()
        }
        data = null
    }

    protected abstract fun createDfeModel(): DfeModel

    override fun onDataChanged() {
        if (data!!.isReady) {
            listener?.onDataChanged()
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        AppLog.e("ErrorResponse: " + error.message, error)
        listener?.onErrorResponse(error)
    }
}
