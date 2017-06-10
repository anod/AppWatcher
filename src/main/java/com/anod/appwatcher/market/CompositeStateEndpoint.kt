package com.anod.appwatcher.market

import com.android.volley.VolleyError

/**
 * @author algavris
 * *
 * @date 27/08/2016.
 */

class CompositeStateEndpoint(val compositeListener: Listener) : CompositeEndpoint(), PlayStoreEndpoint.Listener {
    private var mActiveId = -1

    override fun add(id: Int, endpoint: PlayStoreEndpoint) {
        super.add(id, endpoint)
        if (mActiveId == -1) {
            mActiveId = id
        }
        endpoint.listener = this
    }

    fun active(): PlayStoreEndpointBase {
        return get(mActiveId) as PlayStoreEndpointBase
    }

    fun setActive(id: Int): CompositeStateEndpoint {
        mActiveId = id
        return this
    }

    override fun onDataChanged() {
        compositeListener.onDataChanged(mActiveId, active())
    }

    override fun onErrorResponse(error: VolleyError) {
        compositeListener.onErrorResponse(mActiveId, active(), error)
    }

    override fun startAsync() {
        active().startAsync()
    }

    override fun startSync() {
        active().startSync()
    }

    interface Listener {
        fun onDataChanged(id: Int, endpoint: PlayStoreEndpointBase)
        fun onErrorResponse(id: Int, endpoint: PlayStoreEndpointBase, error: VolleyError)
    }
}
