package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.model.DfeList
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
        private val initialQuery: String
) : ListEndpoint(context, requestQueue, deviceInfoProvider, account) {

    override fun createDfeList(dfeApi: DfeApi, url: String): DfeList {
        return DfeSearch(dfeApi, url)
    }

    override fun createInitialUrl(dfeApi: DfeApi): String {
        return DfeSearch.createSearchUrl(initialQuery)
    }

}
