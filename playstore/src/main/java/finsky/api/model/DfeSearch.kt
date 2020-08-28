package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import finsky.api.DfeApi
import finsky.protos.Messages

class DfeSearch(
        private val dfeApi: DfeApi,
        url: String)
    : DfeList(dfeApi, url, SEARCH) {

    override fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return this.dfeApi.search(url, responseListener, errorListener)
    }

    companion object {
        private const val backendId = 3

        fun createSearchUrl(query: String): String {
            return DfeApi.SEARCH_CHANNEL_URI
                    .buildUpon()
                    .appendQueryParameter("c", backendId.toString())
                    .appendQueryParameter("q", query)
                    .toString()
        }
    }
}
