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
        endpoint.authToken = this.authToken
        endpoint.listener = this.listener
        endpoints.put(id, endpoint)
    }

    fun clear() {
        (0 until endpoints.size()).map { endpoints.valueAt(it) }.forEach { it.listener = null }
        endpoints = SparseArrayCompat()
    }

    override var listener: PlayStoreEndpoint.Listener? = null
        set(listener) {
            (0 until endpoints.size()).map { endpoints.valueAt(it) }.forEach { it.listener = listener }
        }

    override var authToken: String = ""
        set(value) {
            field = value
            (0 until endpoints.size()).map { endpoints.valueAt(it) }.forEach { it.authToken = authToken }
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
