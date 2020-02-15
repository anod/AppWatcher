package info.anodsplace.playstore

import android.util.SparseArray
import finsky.api.model.DfeModel

/**
 * @author Alex Gavrishev
 * *
 * @date 27/08/2016.
 */

class CompositeStateEndpoint() : PlayStoreEndpoint {

    private val endpoints = SparseArray<PlayStoreEndpoint>()

    fun put(id: Int, endpoint: PlayStoreEndpoint) {
        endpoints.put(id, endpoint)
        if (activeId == -1) {
            activeId = id
        }
    }

    operator fun get(id: Int): PlayStoreEndpoint {
        return endpoints.get(id)!!
    }

    val active: PlayStoreEndpoint
        get() = endpoints[activeId]

    var activeId = -1

    fun activate(activeId: Int): CompositeStateEndpoint {
        this.activeId = activeId
        return this
    }

    override var authToken: String = ""
        set(value) {
            field = value
            (0 until endpoints.size()).map { endpoints.valueAt(it) }.forEach { it.authToken = authToken }
        }

    override suspend fun start(): DfeModel {
        return active.start()
    }

    override fun reset() {
        (0 until endpoints.size())
                .map { endpoints.valueAt(it) }
                .forEach { it.reset() }
    }
}
