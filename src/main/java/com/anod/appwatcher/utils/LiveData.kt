package com.anod.appwatcher.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <A, B> LiveData<A>.combineLatest(b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        addSource(this@combineLatest) {
            if (it == null && value != null) value = null
            lastA = it
            if (lastA != null && lastB != null) value = lastA!! to lastB!!
        }

        addSource(b) {
            if (it == null && value != null) value = null
            lastB = it
            if (lastA != null && lastB != null) value = lastA!! to lastB!!
        }
    }
}

fun <A, B, C> LiveData<A>.combineLatest(b: LiveData<B>, c: LiveData<C>): LiveData<Triple<A, B, C>> {
    return MediatorLiveData<Triple<A, B, C>>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null

        addSource(this@combineLatest) {
            if (it == null && value != null) value = null
            lastA = it
            if (lastA != null && lastB != null && lastC != null) value = Triple(lastA!!, lastB!!, lastC!!)
        }

        addSource(b) {
            if (it == null && value != null) value = null
            lastB = it
            if (lastA != null && lastB != null && lastC != null) value = Triple(lastA!!, lastB!!, lastC!!)
        }

        addSource(c) {
            if (it == null && value != null) value = null
            lastC = it
            if (lastA != null && lastB != null && lastC != null) value = Triple(lastA!!, lastB!!, lastC!!)
        }
    }
}

