package com.anod.appwatcher.diagnostics

/** Consent belongs to a selected account, not to the app install or a restored backup. */
class DiagnosticConsent(
    private val account: () -> String?,
    private val hashes: String,
    private val read: () -> String?,
    private val write: (String?) -> Unit
) {
    private var revision = 0L

    val allowed: Boolean
        get() = DiagnosticAccounts.allows(account(), hashes)

    val enabled: Boolean
        @Synchronized get() = allowed && read() == account()?.let(DiagnosticAccounts::hash)

    @Synchronized
    fun setEnabled(enabled: Boolean) {
        revision++
        write(if (enabled && allowed) account()?.let(DiagnosticAccounts::hash) else null)
    }

    @Synchronized
    fun changingAccount(newName: String?) {
        if (newName != account() || !DiagnosticAccounts.allows(newName, hashes)) {
            revision++
            write(null)
        }
    }

    @Synchronized
    fun permit(): Long? = revision.takeIf { enabled }

    @Synchronized
    fun discardQueued() {
        revision++
    }

    // Account mutations and disabling wait for an in-flight append; queued old-account records
    // are discarded, even if the user re-enables recording before the queue drains.
    @Synchronized
    fun withPermit(permit: Long, action: () -> Unit) {
        if (permit == revision && enabled) action()
    }
}