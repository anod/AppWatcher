package finsky.api.model

import android.net.Uri
import finsky.api.DfeApi
import finsky.protos.ResponseWrapper

class DfeSearch(
        private val dfeApi: DfeApi,
        url: String)
    : DfeList(dfeApi, url, SEARCH) {

    override suspend fun makeRequest(url: String): ResponseWrapper {
        return dfeApi.search(url)
    }

    companion object {
        private const val backendId = 3

        fun createSearchUrl(query: String): String {
            return Uri.parse(DfeApi.SEARCH_CHANNEL_URI)
                    .buildUpon()
                    .appendQueryParameter("c", backendId.toString())
                    .appendQueryParameter("q", query)
                    .toString()
        }
    }
}
