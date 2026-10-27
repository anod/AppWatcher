package com.anod.appwatcher.diagnostics

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/** A recorder with consent off and no writer, for tests that do not exercise diagnostics. */
fun disabledDiagnostics(context: Context) = DiagnosticRecorder(
    context,
    DiagnosticConsent({ null }, "", { null }, { }),
    CoroutineScope(Job().apply { cancel() })
)