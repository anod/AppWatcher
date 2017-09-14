package com.google.android.finsky.api.model

import com.android.volley.Request
import com.anod.appwatcher.utils.CollectionsUtils
import com.google.android.finsky.api.DfeApi
import com.google.android.finsky.protos.nano.Messages.ListResponse


class DfeList(private val dfeApi: DfeApi,
              initialListUrl: String,
              autoLoadNextPage: Boolean,
              responseFilter: CollectionsUtils.Predicate<Document>)
    : ContainerList<ListResponse>(initialListUrl, autoLoadNextPage, responseFilter) {

    override fun makeRequest(url: String): Request<*> {
        return this.dfeApi.list(url, this, this)
    }

}