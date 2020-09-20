// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import androidx.collection.SimpleArrayMap
import androidx.lifecycle.MutableLiveData

class SelectionState : Collection<String> {

    class Change(
            val defaultSelected: Boolean,
            val key: String?,
            val selected: Boolean,
            val status: Int,
            val hasSelection: Boolean
    )

    val selectionChange = MutableLiveData<Change>()

    private var defaultSelected: Boolean = false
    private val keysStatus = SimpleArrayMap<String, Int>()
    private val selectedKeys = SimpleArrayMap<String, Boolean>()

    fun selectAll(select: Boolean) {
        selectedKeys.clear()
        defaultSelected = select
        selectionChange.value = Change(select, null, select, STATUS_DEFAULT, isNotEmpty())
    }

    fun selectKey(key: String, select: Boolean) {
        if (select) {
            selectedKeys.put(key, select)
            selectionChange.value = Change(defaultSelected, key, true, getStatus(key), isNotEmpty())
        } else {
            selectedKeys.remove(key)
            selectionChange.value = Change(defaultSelected, key, false, getStatus(key), isNotEmpty())
        }
    }

    fun toggleKey(key: String) {
        if (selectedKeys.containsKey(key)) {
            selectKey(key, !selectedKeys[key]!!)
        } else {
            selectKey(key, true)
        }
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

    override fun isEmpty() = !defaultSelected && selectedKeys.isEmpty

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
}