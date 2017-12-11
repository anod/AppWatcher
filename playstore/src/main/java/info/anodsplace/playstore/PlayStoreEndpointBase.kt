package info.anodsplace.playstore

import android.accounts.Account
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
}

abstract class PlayStoreEndpointBase(
        context: Context,
        private val requestQueue: RequestQueue,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val account: Account) : PlayStoreEndpoint, Response.ErrorListener, OnDataChangedListener {
    protected val context: Context = context.applicationContext

    override var authToken = ""

    override var listener: PlayStoreEndpoint.Listener? = null

    var data: DfeModel? = null
        internal set
    val dfeApi: DfeApi by lazy {
        val deviceId = deviceInfoProvider.deviceId
        val simOperator = deviceInfoProvider.simOperator
        val dfeApiContext = DfeApiContext(context, account, authToken, deviceId, simOperator, ContentLevel().dfeValue)
        DfeApiImpl(requestQueue, dfeApiContext)
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
        if (this.authToken.isBlank()) {
            AppLog.e("Authentication token is empty")
        }
        if (data == null) {
            data = createDfeModel()
            data!!.addDataChangedListener(this)
            data!!.addErrorListener(this)
        }
    }

    override fun reset() {
        data?.unregisterAll()
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
