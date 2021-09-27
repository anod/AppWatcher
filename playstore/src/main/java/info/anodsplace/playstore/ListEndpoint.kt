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
        account: Account
) : PlayStoreEndpoint {
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
        data = createDfeList(dfeApi, nextPageUrl)
        return@withContext data!!
    }

    abstract fun createDfeList(dfeApi: DfeApi, nextPageUrl: String): DfeList
}
