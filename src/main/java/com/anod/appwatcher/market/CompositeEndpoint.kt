package com.anod.appwatcher.market

import android.accounts.Account
import android.support.v4.util.SparseArrayCompat

/**
 * @author algavris
 * *
 * @date 27/08/2016.
 */

open class CompositeEndpoint : PlayStoreEndpoint {
    private var mEndpoints = SparseArrayCompat<PlayStoreEndpoint>()

    operator fun get(id: Int): PlayStoreEndpoint {
        return mEndpoints.get(id)
    }

    open fun add(id: Int, endpoint: PlayStoreEndpoint) {
        mEndpoints.put(id, endpoint)
    }

    fun clear() {
        mEndpoints = SparseArrayCompat<PlayStoreEndpoint>()
    }

    override var listener: PlayStoreEndpoint.Listener?
        get() = this.listener
        set(listener) {
            for (i in 0..mEndpoints.size() - 1) {
                mEndpoints.valueAt(i).listener = listener
            }
        }

    override val authSubToken: String
        get() = if (mEndpoints.size() > 0) mEndpoints.valueAt(0).authSubToken else ""

    override fun setAccount(account: Account, authSubToken: String): PlayStoreEndpoint {
        for (i in 0..mEndpoints.size() - 1) {
            mEndpoints.valueAt(i).setAccount(account, authSubToken)
        }
        return this
    }

    override fun startAsync() {
        for (i in 0..mEndpoints.size() - 1) {
            mEndpoints.valueAt(i).startAsync()
        }
    }

    override fun startSync() {
        for (i in 0..mEndpoints.size() - 1) {
            mEndpoints.valueAt(i).startSync()
        }
    }

    override fun reset() {
        (0..mEndpoints.size() - 1)
                .map { mEndpoints.valueAt(it) }
                .forEach { it.reset() }
    }
}
