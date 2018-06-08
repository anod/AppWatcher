package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue

import finsky.api.model.DfeList
import finsky.api.model.DfeModel

/**
 * @author Alex Gavrishev
 * *
 * @date 16/12/2016.
 */
class WishlistEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, private val autoloadNext: Boolean)
    : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider, account) {

    var listData: DfeList?
        get() = data as? DfeList
        set(value) {
            super.data = value
        }

    override fun reset() {
       listData?.resetItems()
       super.reset()
    }

    val count: Int
        get() = listData?.count ?: 0

    override fun executeAsync() {
        listData?.startLoadItems()
    }

    override fun executeSync() {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun createDfeModel(): DfeModel {
        return DfeList(dfeApi, dfeApi.createLibraryUrl(backendId, libraryId, 7, null), autoloadNext, AppDetailsFilter.predicate)
    }

    companion object {
        private const val libraryId = "u-wl"
        private const val backendId = 0
    }
}
