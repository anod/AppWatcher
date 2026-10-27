package com.anod.appwatcher.diagnostics

import android.content.Context
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/** A recorder with consent off and no writer, for tests that do not exercise diagnostics. */
fun disabledDiagnostics(context: Context): DiagnosticRecorder {
    val scope = CoroutineScope(Job().apply { cancel() })
    return DiagnosticRecorder(context, Preferences(context, NotificationManager.NoOp(), scope, ""), scope)
}