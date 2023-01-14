package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.DfeWishList
import okhttp3.OkHttpClient

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
class WishListEndpoint(
        context: Context,
        http: OkHttpClient,
        deviceInfoProvider: DfeDeviceInfoProvider,
        account: Account,
        authTokenProvider: DfeAuthTokenProvider
) : ListEndpoint(context, http, deviceInfoProvider, account, authTokenProvider) {

    override fun createDfeList(dfeApi: DfeApi, nextPageUrl: String) = DfeWishList(dfeApi, nextPageUrl)
}
