package info.anodsplace.playstore

import android.support.v4.util.SparseArrayCompat

/**
 * @author algavris
 * *
 * @date 27/08/2016.
 */
open class CompositeEndpoint : PlayStoreEndpoint {
    private var endpoints = SparseArrayCompat<PlayStoreEndpoint>()

    operator fun get(id: Int): PlayStoreEndpoint {
        return endpoints.get(id)
    }

    open fun put(id: Int, endpoint: PlayStoreEndpoint) {
        endpoints.put(id, endpoint)
    }

    fun clear() {
        endpoints = SparseArrayCompat()
    }

    override var listener: PlayStoreEndpoint.Listener?
        get() = this.listener
        set(listener) {
            for (i in 0 until endpoints.size()) {
                endpoints.valueAt(i).listener = listener
            }
        }

    override var authToken: String
        get() = if (endpoints.size() > 0) endpoints.valueAt(0).authToken else ""
        set(value) {
            for (i in 0 until endpoints.size()) {
                endpoints.valueAt(i).authToken = authToken
            }
        }

    override fun startAsync() {
        for (i in 0 until endpoints.size()) {
            endpoints.valueAt(i).startAsync()
        }
    }

    override fun startSync() {
        for (i in 0 until endpoints.size()) {
            endpoints.valueAt(i).startSync()
        }
    }

    override fun reset() {
        (0 until endpoints.size())
                .map { endpoints.valueAt(it) }
                .forEach { it.reset() }
    }
}
