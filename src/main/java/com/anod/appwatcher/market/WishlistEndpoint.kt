package com.anod.appwatcher.market

import android.content.Context

import com.google.android.finsky.api.model.DfeList
import com.google.android.finsky.api.model.DfeModel

/**
 * @author algavris
 * *
 * @date 16/12/2016.
 */

class WishlistEndpoint(context: Context, private val mAutoloadNext: Boolean) : PlayStoreEndpointBase(context) {

    var listData: DfeList?
        get() = data as? DfeList
        set(value) {
            super.data = value
        }

    override fun reset() {
       listData?.resetItems()
       super.reset()
    }

    val count: Int
        get() = listData?.count ?: 0

    override fun executeAsync() {
        listData?.startLoadItems()
    }

    override fun executeSync() {
        throw UnsupportedOperationException("Not implemented")
    }


    override fun createDfeModel(): DfeModel {
        return DfeList(dfeApi, dfeApi!!.createLibraryUrl(BACKEND_ID, LIBRARY_ID, 7, null), mAutoloadNext, AppDetailsFilter.predicate)
    }

    companion object {
        private const val LIBRARY_ID = "u-wl"
        private const val BACKEND_ID = 0
    }
}
