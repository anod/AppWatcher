package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.model.DfeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
interface DeviceInfoProvider {
    val deviceId: String
    val simOperator: String
}

abstract class PlayStoreEndpointBase<D : DfeModel>(
        context: Context,
        private val requestQueue: RequestQueue,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val account: Account) : PlayStoreEndpoint {

    protected val context: Context = context.applicationContext
    override var authToken = ""
    var data: D? = null
        internal set

    val dfeApi: DfeApi by lazy {
        DfeApiImpl(requestQueue, context, account, authToken, deviceInfoProvider)
    }

    override suspend fun start(): D = withContext(Dispatchers.Main) {
        init()
        beforeRequest(data!!)
        data!!.execute()
        return@withContext data!!
    }

    protected abstract fun createDfeModel(): D
    abstract fun beforeRequest(data: D)

    internal fun init() {
        if (this.authToken.isBlank()) {
            throw IllegalStateException("Auth token is empty")
        }
        if (data == null) {
            data = createDfeModel()
        }
    }

    override fun reset() {
        data = null
    }

}
