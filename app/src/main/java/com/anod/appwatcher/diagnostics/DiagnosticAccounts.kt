package com.anod.appwatcher.diagnostics

import java.security.MessageDigest
import java.util.Locale

object DiagnosticAccounts {
    fun hash(account: String): String = MessageDigest.getInstance("SHA-256")
        .digest(account.trim().lowercase(Locale.ROOT).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}