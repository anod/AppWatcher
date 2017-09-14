package com.anod.appwatcher.market

import android.accounts.Account
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

    open fun add(id: Int, endpoint: PlayStoreEndpoint) {
        endpoints.put(id, endpoint)
    }

    fun clear() {
        endpoints = SparseArrayCompat<PlayStoreEndpoint>()
    }

    override var listener: PlayStoreEndpoint.Listener?
        get() = this.listener
        set(listener) {
            for (i in 0 until endpoints.size()) {
                endpoints.valueAt(i).listener = listener
            }
        }

    override val authSubToken: String
        get() = if (endpoints.size() > 0) endpoints.valueAt(0).authSubToken else ""

    override fun setAccount(account: Account, authSubToken: String): PlayStoreEndpoint {
        for (i in 0 until endpoints.size()) {
            endpoints.valueAt(i).setAccount(account, authSubToken)
        }
        return this
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
