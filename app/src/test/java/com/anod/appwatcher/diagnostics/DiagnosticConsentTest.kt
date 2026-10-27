package com.anod.appwatcher.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticConsentTest {
    private var selected: String? = "diagnostic-account"
    private var persisted: String? = null
    private val hashes = DiagnosticAccounts.hash("diagnostic-account")

    private fun consent() = DiagnosticConsent({ selected }, hashes, { persisted }, { persisted = it })

    @Test
    fun offByDefaultAndPersistsOnlyExplicitAllowedConsent() {
        val consent = consent()
        assertFalse(consent.enabled)
        assertTrue(consent.allowed)
        consent.setEnabled(true)
        assertTrue(consent.enabled)
        assertTrue(consent().enabled)
        assertEquals(hashes, persisted)
        consent.setEnabled(false)
        assertFalse(consent().enabled)
    }

    @Test
    fun normalizationEmptyAllowlistAndLiteralInjectionValues() {
        assertTrue(DiagnosticAccounts.allows("  DIAGNOSTIC-ACCOUNT ", hashes))
        assertFalse(DiagnosticAccounts.allows(selected, ""))
        assertFalse(DiagnosticAccounts.allows(null, hashes))
        val digest = DiagnosticAccounts.hash("quotes\"\\\n,=injection")
        assertTrue(digest.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun accountMutationRevokesPersistedConsentAndQueuedEvents() {
        val consent = consent()
        consent.setEnabled(true)
        val permit = consent.permit()
        assertNotNull(permit)
        consent.changingAccount("not-allowed")
        selected = "not-allowed"
        assertFalse(consent.enabled)
        assertFalse(consent.allowed)
        consent.setEnabled(true)
        assertFalse(consent().enabled)
        selected = "diagnostic-account"
        assertFalse(consent().enabled)
        consent.setEnabled(true)
        var appended = false
        consent.withPermit(permit!!) { appended = true }
        assertFalse(appended)
        consent.withPermit(consent.permit()!!) { appended = true }
        assertTrue(appended)
    }

    @Test
    fun disablingAndReenablingNeverReplaysTheOldQueue() {
        val consent = consent()
        consent.setEnabled(true)
        val permit = consent.permit()!!
        consent.setEnabled(false)
        consent.setEnabled(true)
        var appended = false
        consent.withPermit(permit) { appended = true }
        assertFalse(appended)
    }

    @Test
    fun sameAccountCredentialRefreshPreservesConsent() {
        val consent = consent()
        consent.setEnabled(true)
        consent.changingAccount(selected)
        assertTrue(consent.enabled)
    }
}