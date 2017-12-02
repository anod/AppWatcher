package info.anodsplace.playstore

import com.android.volley.VolleyError

/**
 * @author algavris
 * *
 * @date 27/08/2016.
 */

class CompositeStateEndpoint(private val compositeListener: Listener) : CompositeEndpoint(), PlayStoreEndpoint.Listener {

    override fun add(id: Int, endpoint: PlayStoreEndpoint) {
        super.add(id, endpoint)
        if (activeId == -1) {
            activeId = id
        }
        endpoint.listener = this
    }

    val active: PlayStoreEndpointBase
        get() = get(activeId) as PlayStoreEndpointBase

    var activeId = -1

    fun activate(activeId: Int): CompositeStateEndpoint {
        this.activeId = activeId
        return this
    }

    override fun onDataChanged() {
        compositeListener.onDataChanged(activeId, active)
    }

    override fun onErrorResponse(error: VolleyError) {
        compositeListener.onErrorResponse(activeId, active, error)
    }

    override fun startAsync() {
        active.startAsync()
    }

    override fun startSync() {
        active.startSync()
    }

    interface Listener {
        fun onDataChanged(id: Int, endpoint: PlayStoreEndpointBase)
        fun onErrorResponse(id: Int, endpoint: PlayStoreEndpointBase, error: VolleyError)
    }
}
