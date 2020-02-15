package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.DfeApiImpl
import finsky.api.model.DfeModel
import finsky.api.model.DfeSearch

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(
        context: Context,
        requestQueue: RequestQueue,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account,
        val query: String,
        private val autoLoadNextPage: Boolean
) : PlayStoreEndpoint {
    override var authToken = ""

    var data: DfeSearch? = null
        internal set

    private val dfeApi: DfeApi by lazy {
        DfeApiImpl(requestQueue, context, account, authToken, deviceInfoProvider)
    }

    override fun reset() {
        data?.resetItems()
        data = null
    }

    val count: Int
        get() = data?.count ?: 0

    override suspend fun start(): DfeModel {
        data = DfeSearch(dfeApi, query, autoLoadNextPage, AppDetailsFilter.predicate).also {
            it.startLoadItems()
        }
        return data!!
    }
}
