package com.anod.appwatcher.market

import android.content.Context

import com.google.android.finsky.api.DfeUtils
import com.google.android.finsky.api.model.DfeModel
import com.google.android.finsky.api.model.DfeSearch

/**
 * @author alex
 * *
 * @date 2015-02-21
 */
class SearchEndpoint(context: Context, private val mAutoLoadNextPage: Boolean) : PlayStoreEndpointBase(context) {
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
        val searchUrl = DfeUtils.formSearchUrl(query, BACKEND_ID)
        return DfeSearch(dfeApi, query, searchUrl, mAutoLoadNextPage, AppDetailsFilter.predicate)
    }

    companion object {
        private val BACKEND_ID = 3
    }
}
