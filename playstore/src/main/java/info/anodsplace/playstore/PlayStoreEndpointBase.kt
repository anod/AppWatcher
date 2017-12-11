package info.anodsplace.playstore

import android.accounts.Account
import android.app.Application
import android.content.Context
import com.android.volley.RequestQueue

import com.android.volley.Response
import com.android.volley.VolleyError
import finsky.api.DfeApi
import finsky.api.DfeApiContext
import finsky.api.DfeApiImpl
import finsky.api.model.DfeModel
import finsky.api.model.OnDataChangedListener
import finsky.config.ContentLevel

import info.anodsplace.android.log.AppLog

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
interface DeviceInfoProvider {
    val deviceId: String
    val simOperator: String
    // App.provide(context).telephonyManager.simOperator
}

abstract class PlayStoreEndpointBase
    constructor(context: Context, private val requestQueue: RequestQueue, private val deviceInfoProvider: DeviceInfoProvider) : PlayStoreEndpoint, Response.ErrorListener, OnDataChangedListener {
    protected val context: Context = context.applicationContext

    final override var authSubToken: String = ""
        private set
    override var listener: PlayStoreEndpoint.Listener? = null

    var data: DfeModel? = null
        internal set
    var dfeApi: DfeApi? = null
        private set
    private var account: Account? = null

    override fun setAccount(account: Account, authSubToken: String): PlayStoreEndpoint {
        this.account = account
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
        if (this.account == null) {
            AppLog.e("Account is empty")
        }
        if (this.authSubToken.isBlank()) {
            AppLog.e("Account authentication token is empty")
        }
        if (dfeApi == null) {
            val deviceId = deviceInfoProvider.deviceId
            val simOperator = deviceInfoProvider.simOperator
            val dfeApiContext = DfeApiContext(context, account, authSubToken, deviceId, simOperator, ContentLevel().dfeValue)
            dfeApi = DfeApiImpl(requestQueue, dfeApiContext)
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
            listener?.onDataChanged(data!!)
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        AppLog.e("ErrorResponse: " + error.message, error)
        listener?.onErrorResponse(error)
    }
}
