package com.anod.appwatcher.diagnostics

import java.security.MessageDigest
import java.util.Locale

object DiagnosticAccounts {
    fun hash(account: String): String = MessageDigest.getInstance("SHA-256")
        .digest(account.trim().lowercase(Locale.ROOT).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

/** SHA-256 hashes of accounts that may record diagnostics. */
class DiagnosticAllowlist(private val hashes: Set<String>) {
    operator fun contains(accountHash: String): Boolean = accountHash in hashes

    companion object {
        fun parse(hashes: String) = DiagnosticAllowlist(hashes.split(',').filter(String::isNotEmpty).toSet())
    }
}