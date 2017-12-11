package finsky.api.model

import android.net.Uri
import com.android.volley.Request
import finsky.api.DfeApi
import finsky.protos.nano.Messages.Search

class DfeSearch(
        private val dfeApi: DfeApi,
        val query: String,
        autoLoadNextPage: Boolean,
        filter: FilterPredicate?)
    : ContainerList<Search.SearchResponse>(createSearchUrl(query, backendId).build().toString(), autoLoadNextPage, filter) {

    override fun makeRequest(url: String): Request<*> {
        return this.dfeApi.search(url, this, this)
    }

    companion object {
        private const val backendId = 3

        fun createSearchUrl(query: String, backendId: Int): Uri.Builder {
            var queryBackendId = backendId
            if (queryBackendId == 9) {
                queryBackendId = 0
            }
            return DfeApi.SEARCH_CHANNEL_URI.buildUpon().appendQueryParameter("c", queryBackendId.toString()).appendQueryParameter("q", query)
        }

    }
}
