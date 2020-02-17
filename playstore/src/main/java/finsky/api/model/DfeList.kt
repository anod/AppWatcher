package finsky.api.model

import com.android.volley.Request
import com.android.volley.Response
import finsky.api.DfeApi
import finsky.protos.Messages
import finsky.protos.Messages.ListResponse


class DfeList(private val dfeApi: DfeApi,
              initialListUrl: String,
              autoLoadNextPage: Boolean,
              filter: ((Document?) -> Boolean))
    : ContainerList<ListResponse>(initialListUrl, autoLoadNextPage, filter) {

    override val url: String
        get() = ""

    override fun makeRequest(url: String, responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener): Request<*> {
        return this.dfeApi.list(url, responseListener, errorListener)
    }

}