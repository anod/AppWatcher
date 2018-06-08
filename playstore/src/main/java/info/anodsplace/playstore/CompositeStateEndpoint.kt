package info.anodsplace.playstore

import com.android.volley.VolleyError
import finsky.api.model.DfeModel

/**
 * @author Alex Gavrishev
 * *
 * @date 27/08/2016.
 */

class CompositeStateEndpoint(private val compositeListener: Listener) : CompositeEndpoint(), PlayStoreEndpoint.Listener {

    override fun put(id: Int, endpoint: PlayStoreEndpoint) {
        super.put(id, endpoint)
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

    override fun onDataChanged(data: DfeModel) {
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
