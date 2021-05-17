package com.anod.appwatcher.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.collect(scope: CoroutineScope, crossinline action: suspend (value: T) -> Unit): Job {
    return scope.launch {
        collect {
            action(it)
        }
    }
}