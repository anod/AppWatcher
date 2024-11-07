// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import androidx.compose.runtime.Immutable
import info.anodsplace.ktx.equalsHash
import info.anodsplace.ktx.hashCodeOf

typealias SelectionStateKeyExtras = Map<String, String>

@Immutable
data class SelectionState(
    val defaultSelected: Boolean = false,
    val extras: Map<String, SelectionStateKeyExtras> = emptyMap(),
    val selectedKeys: Map<String, Boolean> = emptyMap(),
    val selectedCount: Int = 0
) : Collection<String> {

    override fun hashCode(): Int = hashCodeOf(defaultSelected, extras, selectedKeys, selectedCount)

    override fun equals(other: Any?) = equalsHash(this, other)

    fun selectAll(select: Boolean): SelectionState = copy(selectedKeys = emptyMap(), defaultSelected = select, selectedCount = 0)

    fun selectKey(key: String, select: Boolean, selectExtra: SelectionStateKeyExtras = emptyMap()): SelectionState {
        var newSelectedCount = selectedCount
        if (select) {
            newSelectedCount += 1
        } else if (selectedKeys.containsKey(key)) {
            newSelectedCount -= 1
            if (newSelectedCount < 0) {
                newSelectedCount = 0
            }
        }

        val newSelectedKeys = selectedKeys.toMutableMap()
        newSelectedKeys[key] = select

        val newExtras = extras.toMutableMap()
        newExtras[key] = getExtra(key) + selectExtra
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

    fun toggleKey(key: String, selectExtra: SelectionStateKeyExtras = emptyMap()): SelectionState {
        return if (selectedKeys.containsKey(key)) {
            selectKey(key, !selectedKeys[key]!!, selectExtra)
        } else {
            selectKey(key, !defaultSelected, selectExtra)
        }
    }

    fun getExtra(key: String): SelectionStateKeyExtras {
        if (extras.containsKey(key)) {
            return extras[key] ?: emptyMap()
        }
        return emptyMap()
    }

    fun mergeExtras(new: Map<String, SelectionStateKeyExtras>): SelectionState {
        if (extras.isEmpty()) {
            return copy(extras = new)
        }
        val temp = extras.toMutableMap()
        new.keys.forEach { newKey ->
            val newExtra = new[newKey] ?: emptyMap()
            if (newExtra.isNotEmpty()) {
                val existingBundle = temp[newKey] ?: emptyMap()
                if (existingBundle.isEmpty()) {
                    temp[newKey] = newExtra
                } else {
                    temp[newKey] = existingBundle.toMutableMap().apply {
                        putAll(newExtra)
                    }
                }
            }
        }
        return copy(extras = temp)
    }

    fun setExtras(newExtras: Map<String, SelectionStateKeyExtras>): SelectionState {
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

fun SelectionState.filterWithExtra(extra: (SelectionStateKeyExtras) -> Boolean): List<String> {
    return iterator()
        .asSequence()
        .filter { key -> extra(getExtra(key)) }
        .toList()
}