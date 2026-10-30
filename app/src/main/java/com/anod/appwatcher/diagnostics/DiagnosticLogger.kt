package com.anod.appwatcher.diagnostics

import info.anodsplace.applog.AppLog

class DiagnosticLogger(
    val delegate: AppLog.Logger,
    private val sink: (Int, String, String) -> Unit
) : AppLog.Logger {
    override fun isLoggable(tag: String, level: Int): Boolean = delegate.isLoggable(tag, level)

    override fun getStackTraceString(tr: Throwable?): String = delegate.getStackTraceString(tr)

    override fun println(priority: Int, tag: String, msg: String) {
        // Neither the optional sink nor a failed delegate may interrupt application work.
        try {
            delegate.println(priority, tag, msg)
        } catch (_: Exception) {
        }
        try {
            sink(priority, tag, msg)
        } catch (_: Exception) {
        }
    }
}