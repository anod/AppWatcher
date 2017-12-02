package info.anodsplace.playstore

import android.content.Context
import com.android.volley.RequestQueue

import finsky.api.DfeUtils
import finsky.api.model.DfeModel
import finsky.api.model.DfeSearch

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, private val autoLoadNextPage: Boolean)
        : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider) {
    var query: String = ""

    var searchData: DfeSearch?
        get() = data as? DfeSearch
        set(value) {
            this.data = value
        }

    override fun reset() {
        searchData?.resetItems()
        super.reset()
    }

    val count: Int
        get() = searchData?.count ?: 0

    override fun executeAsync() {
        searchData?.startLoadItems()
    }

    override fun executeSync() {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun createDfeModel(): DfeModel {
        val searchUrl = DfeUtils.formSearchUrl(query, backendId)
        return DfeSearch(dfeApi!!, query, searchUrl, autoLoadNextPage, AppDetailsFilter.predicate)
    }

    companion object {
        private const val backendId = 3
    }
}
