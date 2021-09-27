package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.model.DfeList
import finsky.api.model.DfeSearch
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(
        context: Context,
        http: OkHttpClient,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account,
        private val initialQuery: String
) : ListEndpoint(context, http, deviceInfoProvider, account) {

    override fun createDfeList(dfeApi: DfeApi, nextPageUrl: String): DfeList {
        return DfeSearch(dfeApi, initialQuery, nextPageUrl)
    }
}
