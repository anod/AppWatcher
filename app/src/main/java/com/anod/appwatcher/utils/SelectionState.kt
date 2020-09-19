// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import androidx.collection.SimpleArrayMap
import androidx.lifecycle.MutableLiveData

class SelectionState {

    class Change(
            val defaultSelected: Boolean,
            val key: String?,
            val selected: Boolean,
            val status: Int,
            val hasSelection: Boolean
    )

    val count: Int
        get() = if (defaultSelected) -1 else selectedKeys.size()

    val hasSelection: Boolean
        get() = defaultSelected || !selectedKeys.isEmpty

    val selectedKeys = SimpleArrayMap<String, Boolean>()
    val selectionChange = MutableLiveData<Change>()

    private var defaultSelected: Boolean = false
    private val keysStatus = SimpleArrayMap<String, Int>()

    fun selectAll(select: Boolean) {
        selectedKeys.clear()
        defaultSelected = select
        selectionChange.value = Change(select, null, select, STATUS_DEFAULT, hasSelection)
    }

    fun selectKey(key: String, select: Boolean) {
        if (select) {
            selectedKeys.put(key, select)
            selectionChange.value = Change(defaultSelected, key, true, getStatus(key), hasSelection)
        } else {
            selectedKeys.remove(key)
            selectionChange.value = Change(defaultSelected, key, false, getStatus(key), hasSelection)
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