package com.anod.appwatcher.accounts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun CoroutineScope.launchAccountInitialization(
    previousJob: Job?,
    userInitiated: Boolean,
    initialize: suspend () -> Unit
): Job? {
    if (!userInitiated && previousJob?.isActive == true) {
        return previousJob
    }
    if (userInitiated) {
        previousJob?.cancel()
    }
    return launch {
        awaitPreviousInitialization(previousJob)
        initialize()
    }
}

internal suspend fun awaitPreviousInitialization(previousJob: Job?) {
    // Keep canceled intermediate jobs waiting so later selections cannot overtake an active session.
    withContext(NonCancellable) {
        previousJob?.join()
    }
    currentCoroutineContext().ensureActive()
}