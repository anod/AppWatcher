package com.anod.appwatcher.utils

import androidx.lifecycle.SavedStateHandle

fun SavedStateHandle.getInt(key: String, defaultValue: Int = 0): Int {
    return get<Any?>(key)?.let { value ->
        (value as? Int) ?: (value as? String)?.toIntOrNull()
    } ?: defaultValue
}