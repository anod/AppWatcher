package finsky.api.model

import android.net.Uri
import com.android.volley.Request
import com.android.volley.Response
import finsky.api.DfeApi
import finsky.protos.nano.Messages
import finsky.protos.nano.Messages.Search

class DfeSearch(
        private val dfeApi: DfeApi,
        val query: String,
        autoLoadNextPage: Boolean,
        filter: FilterPredicate?)
    : ContainerList<Search.SearchResponse>(createSearchUrl(query, backendId).build().toString(), autoLoadNextPage, filter) {

    override val url: String
        get() = ""

    override fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return this.dfeApi.search(url, responseListener, errorListener)
    }

    companion object {
        private const val backendId = 3

        fun createSearchUrl(query: String, backendId: Int): Uri.Builder {
            var queryBackendId = backendId
            if (queryBackendId == 9) {
                queryBackendId = 0
            }
            return DfeApi.SEARCH_CHANNEL_URI
                    .buildUpon()
                    .appendQueryParameter("c", queryBackendId.toString())
                    .appendQueryParameter("q", query)
        }
    }
}
