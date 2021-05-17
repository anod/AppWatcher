package com.anod.appwatcher.utils

import kotlinx.coroutines.flow.MutableSharedFlow

typealias EventFlow<T> = MutableSharedFlow<T>

fun <T> EventFlow() = MutableSharedFlow<T>(extraBufferCapacity = 1)
