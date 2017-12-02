package finsky.api.model

import com.android.volley.Request
import finsky.api.DfeApi
import finsky.protos.nano.Messages.Search

class DfeSearch(
        private val dfeApi: DfeApi,
        val query: String, initialUrl: String,
        autoLoadNextPage: Boolean,
        filter: FilterPredicate?)
    : ContainerList<Search.SearchResponse>(initialUrl, autoLoadNextPage, filter) {

    override fun makeRequest(url: String): Request<*> {
        return this.dfeApi.search(url, this, this)
    }
}
