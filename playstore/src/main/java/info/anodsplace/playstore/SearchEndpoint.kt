package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.DfeList
import finsky.api.DfeSearch
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(
    context: Context,
    http: OkHttpClient,
    deviceInfoProvider: DfeDeviceInfoProvider,
    account: Account,
    authTokenProvider: DfeAuthTokenProvider,
    private val initialQuery: String
) : ListEndpoint(context, http, deviceInfoProvider, account, authTokenProvider) {

    override fun createDfeList(dfeApi: DfeApi, nextPageUrl: String): DfeList {
        return DfeSearch(dfeApi, initialQuery, nextPageUrl)
    }
}
