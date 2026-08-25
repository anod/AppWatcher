package com.anod.appwatcher

import com.anod.appwatcher.sync.SyncFailureException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import finsky.api.DfeError
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

internal object CrashlyticsExceptionFilter {
    fun shouldIgnore(tr: Throwable, isNetworkException: (Throwable) -> Boolean): Boolean {
        if (tr is SyncFailureException) {
            return false
        }
        val root = rootCause(tr)
        return root is CancellationException ||
            root is UserRecoverableAuthException ||
            root is UserRecoverableAuthIOException ||
            root is DfeError ||
            root.javaClass.name == "android.system.GaiException" ||
            root is UnknownHostException ||
            (root is IOException && root.message?.contains("NetworkError") == true) ||
            isNetworkException(root)
    }

    private fun rootCause(tr: Throwable): Throwable {
        var current = tr
        val seen = mutableSetOf<Throwable>()
        repeat(MAX_CAUSE_DEPTH) {
            val cause = current.cause
            if (cause == null || !seen.add(cause)) {
                return current
            }
            current = cause
        }
        return current
    }

    private const val MAX_CAUSE_DEPTH = 16
}