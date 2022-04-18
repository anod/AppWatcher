package com.anod.appwatcher.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

class PackageChangedReceiver {
    private val subject = MutableSharedFlow<String?>()
    val observer: Flow<String> = subject.filterNotNull().distinctUntilChanged()

    suspend fun emit(packageName: String) {
        subject.emit(packageName)
    }
}