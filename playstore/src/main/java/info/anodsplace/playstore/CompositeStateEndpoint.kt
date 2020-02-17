package info.anodsplace.playstore

import android.util.SparseArray
import finsky.api.model.DfeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Alex Gavrishev
 * *
 * @date 27/08/2016.
 */

class CompositeStateEndpoint() : PlayStoreEndpoint {

    private val endpoints = SparseArray<PlayStoreEndpoint>()
    var onStart: ((Int, PlayStoreEndpoint) -> Unit)? = null

    fun put(id: Int, endpoint: PlayStoreEndpoint) {
        endpoints.put(id, endpoint)
        if (activeId == -1) {
            activeId = id
        }
    }

    operator fun get(id: Int): PlayStoreEndpoint {
        return endpoints.get(id)!!
    }

    private val active: PlayStoreEndpoint
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

    override suspend fun start(): DfeModel = withContext(Dispatchers.Main) {
        val model = active.start()
        onStart?.invoke(activeId, active)
        return@withContext model
    }

    override fun reset() {
        (0 until endpoints.size())
                .map { endpoints.valueAt(it) }
                .forEach { it.reset() }
    }
}
