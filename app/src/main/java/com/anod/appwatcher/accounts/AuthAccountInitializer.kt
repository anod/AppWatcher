package com.anod.appwatcher.accounts

import android.accounts.Account
import android.os.Build
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeApi
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthTokenUnavailableException : IllegalStateException("Unable to retrieve authentication token")
class AccountSessionBusyException : IllegalStateException("A Play Store synchronization is in progress")

class PlaySessionCoordinator {
    private val sessionMutex = Mutex()

    suspend fun <T> withSession(action: suspend () -> T): T = sessionMutex.withLock {
        action()
    }

    suspend fun <T> withUserInitiatedSession(action: suspend () -> T): T {
        if (!sessionMutex.tryLock()) {
            throw AccountSessionBusyException()
        }
        return try {
            action()
        } finally {
            sessionMutex.unlock()
        }
    }
}

class AuthAccountInitializer(
    private val preferences: Preferences,
    private val authToken: AuthTokenBlocking,
    private val dfeApi: DfeApi,
    private val playSessionCoordinator: PlaySessionCoordinator
) {
    private val deviceRegistration = DeviceRegistration(
        preferences = preferences,
        dfeApi = dfeApi,
        sdkInt = Build.VERSION.SDK_INT
    )

    suspend fun initialize(
        account: Account,
        userInitiated: Boolean
    ): AuthAccount =
        if (userInitiated) {
            playSessionCoordinator.withUserInitiatedSession {
                initializeInSession(account, userInitiated = true)
            }
        } else {
            playSessionCoordinator.withSession {
                initializeInSession(account, userInitiated = false)
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
        playSessionCoordinator.withSession {
            refreshInSession()
        }
    }

    internal suspend fun refreshInSession(): AuthAccount {
        val account = preferences.account ?: throw IllegalStateException("Account should not be null")
        val androidAccount = account.toAndroidAccount()
        requireToken(authToken.refreshToken(androidAccount))
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