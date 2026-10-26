package com.anod.appwatcher.accounts

import android.accounts.Account
import android.os.Build
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeApi
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AuthTokenUnavailableException : IllegalStateException("Unable to retrieve authentication token")
class AccountSessionBusyException : IllegalStateException("A Play Store synchronization is in progress")
class PlaySessionWaitTimeoutException(message: String) : IllegalStateException(message)

class PlaySessionCoordinator {
    private class Holder(
        val owner: String,
        val acquiredAt: Long
    ) {
        @Volatile
        var stage: String = "started"

        @Volatile
        var stageAt: Long = acquiredAt
    }

    private val sessionMutex = Mutex()

    @Volatile
    private var holder: Holder? = null

    suspend fun <T> withSession(action: suspend () -> T): T = withSession(owner = "session", action = action)

    suspend fun <T> withSession(owner: String, action: suspend () -> T): T {
        sessionMutex.lock()
        return holdSession(owner, action)
    }

    /**
     * Waits at most [waitTimeoutMillis] for the session and throws [PlaySessionWaitTimeoutException]
     * describing the current holder instead of waiting behind a stalled session indefinitely.
     */
    suspend fun <T> withSession(
        owner: String,
        waitTimeoutMillis: Long,
        action: suspend () -> T
    ): T {
        val waitStartedAt = System.currentTimeMillis()
        withTimeoutOrNull(waitTimeoutMillis) { sessionMutex.lock() }
            ?: throw PlaySessionWaitTimeoutException(
                "$owner waited ${System.currentTimeMillis() - waitStartedAt}ms for Play session; ${describeHolder()}"
            )
        return holdSession(owner, action)
    }

    suspend fun <T> withUserInitiatedSession(action: suspend () -> T): T {
        if (!sessionMutex.tryLock()) {
            AppLog.w("User session rejected; ${describeHolder()}", "PlaySession")
            throw AccountSessionBusyException()
        }
        return holdSession("user-initiated", action)
    }

    fun updateStage(stage: String) {
        val current = holder ?: return
        val now = System.currentTimeMillis()
        AppLog.i(
            "${current.owner} stage $stage (previous=${current.stage} ${now - current.stageAt}ms, " +
                "total=${now - current.acquiredAt}ms)",
            "PlaySession"
        )
        current.stage = stage
        current.stageAt = now
    }

    fun describeHolder(): String {
        val current = holder ?: return "holder=none"
        val now = System.currentTimeMillis()
        return "holder=${current.owner}; heldMs=${now - current.acquiredAt}; " +
            "stage=${current.stage}; stageMs=${now - current.stageAt}"
    }

    private suspend fun <T> holdSession(owner: String, action: suspend () -> T): T {
        holder = Holder(owner, System.currentTimeMillis())
        return try {
            action()
        } finally {
            holder = null
            sessionMutex.unlock()
        }
    }
}

class AuthAccountInitializer(
    private val preferences: Preferences,
    private val authToken: AuthTokenBlocking,
    private val dfeApi: DfeApi,
    private val playSessionCoordinator: PlaySessionCoordinator,
    private val deviceRegistrationNotification: DeviceRegistrationNotification
) {
    private val initializations = mutableSetOf<Job>()

    private val deviceRegistration = DeviceRegistration(
        preferences = preferences,
        dfeApi = dfeApi,
        sdkInt = Build.VERSION.SDK_INT
    )

    suspend fun initialize(
        account: Account,
        userInitiated: Boolean
    ): AuthAccount {
        val context = currentCoroutineContext()
        context.ensureActive()
        val job = checkNotNull(context[Job]) {
            "Account initialization requires an active coroutine job"
        }
        val previousJobs = synchronized(this) {
            val previous = initializations.toList()
            if (userInitiated) {
                previous.forEach { it.cancel() }
            }
            initializations.add(job)
            previous
        }
        return try {
            // Canceled initializations may still be finishing device registration.
            withContext(NonCancellable) {
                previousJobs.joinAll()
            }
            context.ensureActive()
            val initializedAccount = if (userInitiated) {
                playSessionCoordinator.withUserInitiatedSession {
                    initializeInSession(account, userInitiated = true)
                }
            } else {
                playSessionCoordinator.withSession(owner = "account-initialization") {
                    initializeInSession(account, userInitiated = false)
                }
            }
            if (!preferences.isDeviceRegistrationRequired) {
                deviceRegistrationNotification.cancel()
            }
            initializedAccount
        } finally {
            synchronized(this) {
                initializations.remove(job)
            }
        }
    }

    private suspend fun initializeInSession(
        account: Account,
        userInitiated: Boolean
    ): AuthAccount {
        val activeAccount = preferences.account
        if (
            !userInitiated &&
            activeAccount != null &&
            (activeAccount.name != account.name || activeAccount.type != account.type)
        ) {
            return activeAccount
        }
        val existingAccount = activeAccount
        val selectedAccount = if (
            existingAccount?.name == account.name &&
            existingAccount.type == account.type
        ) {
            existingAccount
        } else {
            AuthAccount(account, preferences.deviceIdentity, "")
        }
        val authorizeRegistration = userInitiated && selectedAccount.gfsId.isEmpty()
        check(
            preferences.saveAccount(
                selectedAccount,
                deviceRegistrationPending = null,
                deviceRegistrationAuthorized = if (userInitiated) authorizeRegistration else null,
                deviceConfigRevision = null
            )
        ) {
            "Unable to persist selected account"
        }
        val allowCheckIn = userInitiated || preferences.isDeviceRegistrationAuthorized

        try {
            requireToken(authToken.refreshToken(account))
        } catch (e: AuthTokenStartIntent) {
            throw e
        } catch (e: Exception) {
            clearDeviceRegistrationAuthorization()
            AppLog.d("Exception during token refresh. Persisting account anyway, ${e.message}")
            throw e
        }

        clearDeviceRegistrationAuthorization()
        val authAccount = deviceRegistration.ensure(
            selectedAccount,
            allowCheckIn = allowCheckIn
        )
        return authAccount
    }

    suspend fun refresh() {
        playSessionCoordinator.withSession(owner = "account-refresh") {
            refreshInSession()
        }
    }

    internal suspend fun refreshInSession(): AuthAccount {
        val account = preferences.account ?: throw IllegalStateException("Account should not be null")
        val androidAccount = account.toAndroidAccount()
        playSessionCoordinator.updateStage("auth-token")
        requireToken(authToken.refreshToken(androidAccount))
        playSessionCoordinator.updateStage("device-registration")
        return deviceRegistration.ensure(account, allowCheckIn = false)
    }

    private fun requireToken(result: CheckTokenResult) {
        if (result !is CheckTokenResult.Success) {
            throw AuthTokenUnavailableException()
        }
    }

    internal suspend fun clearDeviceRegistrationAuthorization() {
        if (preferences.isDeviceRegistrationAuthorized) {
            check(preferences.saveDeviceRegistrationAuthorization(false)) {
                "Unable to persist device registration authorization"
            }
        }
    }
}