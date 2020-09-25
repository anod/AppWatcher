// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.os.Bundle
import androidx.collection.SimpleArrayMap
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData

class SelectionState : Collection<String> {

    class Change(
            val defaultSelected: Boolean,
            val key: String?,
            val selected: Boolean,
            val extras: Bundle
    )

    val selectionChange = MutableLiveData<Change>()

    private var defaultSelected: Boolean = false
    private val extras = SimpleArrayMap<String, Bundle>()
    private val selectedKeys = SimpleArrayMap<String, Boolean>()
    private var selectedCount = 0

    fun selectAll(select: Boolean) {
        selectedKeys.clear()
        defaultSelected = select
        selectionChange.value = Change(select, null, select, bundleOf("hasSelection" to isNotEmpty()))
    }

    fun selectKey(key: String, select: Boolean, selectExtra: Bundle = Bundle.EMPTY) {
        val allExtras = bundleOf(
                "hasSelection" to isNotEmpty()
        )
        allExtras.putAll(getExtra(key))
        allExtras.putAll(selectExtra)
        if (select) {
            selectedCount += 1
        } else {
            selectedCount -= 1
            if (selectedCount < 0) {
                selectedCount = 0
            }
        }
        selectedKeys.put(key, select)
        selectionChange.value = Change(defaultSelected, key, true, allExtras)
    }

    fun toggleKey(key: String, selectExtra: Bundle) {
        if (selectedKeys.containsKey(key)) {
            selectKey(key, !selectedKeys[key]!!, selectExtra)
        } else {
            selectKey(key, !defaultSelected, selectExtra)
        }
    }

    fun getExtra(key: String): Bundle {
        if (extras.containsKey(key)) {
            return extras.get(key) ?: Bundle.EMPTY
        }
        return Bundle.EMPTY
    }

    fun setExtra(key: String, extra: Bundle) {
        extras.put(key, extra)
    }

    override val size: Int
        get() = if (defaultSelected) -1 else selectedKeys.size()

    override fun contains(element: String): Boolean {
        if (selectedKeys.containsKey(element)) {
            return selectedKeys.get(element) ?: false
        }
        return defaultSelected
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        for (element in elements) {
            if (!selectedKeys.containsKey(element)) {
                return false
            }
        }
        return true
    }

    override fun isEmpty(): Boolean {
        if (defaultSelected) {
            return false
        }
        return selectedCount == 0
    }

    override fun iterator(): Iterator<String> {
        val size = selectedKeys.size()
        var current = 0
        return object : Iterator<String> {
            override fun hasNext() = current < size

            override fun next(): String {
                val next = selectedKeys.keyAt(current)
                current += 1
                return next
            }
        }
    }

    fun clear() {
        selectedKeys.clear()
        selectedCount = 0
        defaultSelected = false
        extras.clear()
        selectionChange.value = Change(false, null, false, bundleOf("hasSelection" to false))
    }
}