package com.google.android.finsky.api.model

import com.android.volley.Request
import com.google.android.finsky.api.DfeApi
import com.google.android.finsky.protos.nano.Messages.ListResponse


class DfeList(private val dfeApi: DfeApi,
              initialListUrl: String,
              autoLoadNextPage: Boolean,
              filter: ((Document?) -> Boolean))
    : ContainerList<ListResponse>(initialListUrl, autoLoadNextPage, filter) {

    override fun makeRequest(url: String): Request<*> {
        return this.dfeApi.list(url, this, this)
    }

}