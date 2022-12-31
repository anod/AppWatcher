// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import info.anodsplace.ktx.equalsHash
import info.anodsplace.ktx.hashCodeOf

data class SelectionState(
      val defaultSelected: Boolean = false,
      private val extras: Map<String, Bundle> = emptyMap(),
      private val selectedKeys: Map<String, Boolean> = emptyMap(),
      val selectedCount: Int = 0
) : Collection<String> {

    override fun hashCode(): Int = hashCodeOf(defaultSelected, extras, selectedKeys, selectedCount)

    override fun equals(other: Any?) = equalsHash(this, other)

    fun selectAll(select: Boolean): SelectionState = copy(selectedKeys = emptyMap(), defaultSelected = select, selectedCount = 0)

    fun selectKey(key: String, select: Boolean, selectExtra: Bundle = Bundle.EMPTY): SelectionState {
        val newSelectedKeys = selectedKeys.toMutableMap()
        newSelectedKeys[key] = select
        var newSelectedCount = selectedCount
        if (select) {
            newSelectedCount += 1
        } else {
            newSelectedCount -= 1
            if (newSelectedCount < 0) {
                newSelectedCount = 0
            }
        }

        val allExtras = bundleOf(
            "hasSelection" to isNotEmpty()
        )
        allExtras.putAll(getExtra(key))
        allExtras.putAll(selectExtra)
        val newExtras = extras.toMutableMap()
        newExtras[key] = allExtras
        return copy(selectedKeys = newSelectedKeys, selectedCount = newSelectedCount, extras = newExtras, defaultSelected = false)
    }

    fun selectKeys(keys: List<String>, select: Boolean, overrideExisting: Boolean = true): SelectionState {
        if (keys.isEmpty()) {
            return this
        }
        if (!overrideExisting && defaultSelected) {
            return this
        }
        val newSelectedKeys = selectedKeys.toMutableMap()
        var newSelectedCount = selectedCount
        var changed = false
        for (key in keys) {
            if (!overrideExisting) {
                if (newSelectedKeys.containsKey(key)) {
                    continue
                }
            }
            newSelectedKeys[key] = select
            changed = true
            if (select) {
                newSelectedCount += 1
            } else {
                newSelectedCount -= 1
                if (newSelectedCount < 0) {
                    newSelectedCount = 0
                }
            }
        }
        if (!changed && !overrideExisting) {
            return this
        }
        return copy(selectedKeys = newSelectedKeys, selectedCount = newSelectedCount, defaultSelected = false)
    }

    fun toggleKey(key: String, selectExtra: Bundle = Bundle.EMPTY): SelectionState {
        return if (selectedKeys.containsKey(key)) {
            selectKey(key, !selectedKeys[key]!!, selectExtra)
        } else {
            selectKey(key, !defaultSelected, selectExtra)
        }
    }

    private fun getExtra(key: String): Bundle {
        if (extras.containsKey(key)) {
            return extras[key] ?: Bundle.EMPTY
        }
        return Bundle.EMPTY
    }

    fun setExtras(newExtras: Map<String, Bundle>): SelectionState {
        return copy(extras = newExtras)
    }

    override val size: Int
        get() = if (defaultSelected) -1 else selectedKeys.size

    override fun contains(element: String): Boolean {
        if (selectedKeys.containsKey(element)) {
            return selectedKeys[element] ?: false
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
        return selectedKeys.keys.iterator()
    }

    fun clear(): SelectionState {
        return copy(
            defaultSelected = false,
            selectedCount = 0,
            extras = emptyMap(),
            selectedKeys = emptyMap()
        )
    }
}

fun SelectionState.filter(selected: Boolean): List<String> {
    return iterator()
        .asSequence()
        .filter { if (selected) contains(it) else !contains(it) }
        .toList()
}
