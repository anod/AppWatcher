package finsky.api.model

import com.android.volley.Request
import finsky.api.DfeApi
import finsky.protos.nano.Messages.ListResponse


class DfeList(private val dfeApi: DfeApi,
              initialListUrl: String,
              autoLoadNextPage: Boolean,
              filter: ((Document?) -> Boolean))
    : ContainerList<ListResponse>(initialListUrl, autoLoadNextPage, filter) {

    override fun makeRequest(url: String): Request<*> {
        return this.dfeApi.list(url, this, this)
    }

}