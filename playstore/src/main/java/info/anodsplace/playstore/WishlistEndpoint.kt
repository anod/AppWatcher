package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.DfeApi
import finsky.api.model.DfeList

/**
 * @author Alex Gavrishev
 * @date 16/12/2016.
 */
class WishListEndpoint(
        context: Context,
        requestQueue: RequestQueue,
        deviceInfoProvider: DeviceInfoProvider,
        account: Account) : ListEndpoint(context, requestQueue, deviceInfoProvider, account) {

    override fun createDfeList(dfeApi: DfeApi, url: String) = DfeList(dfeApi, url)

    override fun createInitialUrl(dfeApi: DfeApi) = dfeApi.createLibraryUrl(backendId, libraryId, 7, null)

    companion object {
        private const val libraryId = "u-wl"
        private const val backendId = 0
    }
}
