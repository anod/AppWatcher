package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.DfeApi
import finsky.api.model.DfeWishList
import okhttp3.OkHttpClient

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
class WishListEndpoint(
        context: Context,
        http: OkHttpClient,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account) : ListEndpoint(context, http, deviceInfoProvider, account) {

    override fun createDfeList(dfeApi: DfeApi, nextPageUrl: String) = DfeWishList(dfeApi, nextPageUrl)
}
