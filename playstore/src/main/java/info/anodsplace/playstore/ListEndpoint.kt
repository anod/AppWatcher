package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
abstract class ListEndpoint(
        context: Context,
        http: OkHttpClient,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account) : PlayStoreEndpoint {
    override var authToken = ""

    private val dfeApi: DfeApi by lazy {
        DfeApiImpl(http, context, account, authToken, deviceInfoProvider)
    }

    open var data: DfeList? = null
        internal set

    override fun reset() {
        data = null
    }

    val count: Int
        get() = data?.count ?: 0

    var nextPageUrl = ""

    override suspend fun start(): DfeList = withContext(Dispatchers.Main) {
        val url = if (nextPageUrl.isEmpty()) createInitialUrl(dfeApi) else nextPageUrl
        data = createDfeList(dfeApi, url)
        return@withContext data!!
    }

    open fun createDfeList(dfeApi: DfeApi, url: String): DfeList {
        return DfeList(dfeApi, url)
    }

    abstract fun createInitialUrl(dfeApi: DfeApi): String
}
