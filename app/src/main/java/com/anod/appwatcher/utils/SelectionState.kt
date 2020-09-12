// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import androidx.collection.SimpleArrayMap

class SelectionState {
    val hasSelection: Boolean
        get() = defaultSelected || !selectedKeys.isEmpty

    val selectedKeys = SimpleArrayMap<String, Boolean>()
    private var defaultSelected: Boolean = false
    private val keysStatus = SimpleArrayMap<String, Int>()

    fun selectAll(select: Boolean) {
        selectedKeys.clear()
        defaultSelected = select
    }

    fun selectKey(key: String, select: Boolean) {
        if (select) {
            selectedKeys.put(key, select)
        } else {
            selectedKeys.remove(key)
        }
    }

    fun toggleKey(key: String) {
        if (selectedKeys.containsKey(key)) {
            selectKey(key, !selectedKeys[key]!!)
        } else {
            selectKey(key, true)
        }
    }

    fun isSelected(key: String): Boolean {
        if (selectedKeys.containsKey(key)) {
            return selectedKeys.get(key) ?: false
        }
        return defaultSelected
    }

    fun getStatus(key: String): Int {
        if (keysStatus.containsKey(key)) {
            return keysStatus.get(key) ?: STATUS_DEFAULT
        }
        return STATUS_DEFAULT
    }

    fun setStatus(key: String, status: Int) {
        keysStatus.put(key, status)
    }

    companion object {
        const val STATUS_DEFAULT = 0
    }
}