package com.anod.appwatcher.accounts

import android.accounts.Account
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeApi
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthTokenUnavailableException : IllegalStateException("Unable to retrieve authentication token")
class AccountSessionBusyException : IllegalStateException("A Play Store synchronization is in progress")

class AuthAccountInitializer(private val preferences: Preferences, private val authToken: AuthTokenBlocking, private val dfeApi: DfeApi) {
    private val deviceRegistration = DeviceRegistration(preferences, dfeApi)
    private val accountMutex = Mutex()

    suspend fun initialize(
        account: Account,
        userInitiated: Boolean = false
    ): AuthAccount {
        if (!userInitiated) {
            return accountMutex.withLock {
                initializeLocked(account, userInitiated = false)
            }
        }
        if (!accountMutex.tryLock()) {
            throw AccountSessionBusyException()
        }
        return try {
            initializeLocked(account, userInitiated = true)
        } finally {
            accountMutex.unlock()
        }
    }

    private suspend fun initializeLocked(
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
                deviceRegistrationAuthorized = if (userInitiated) authorizeRegistration else null
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
        withRefreshedAccount { }
    }

    internal suspend fun <T> withRefreshedAccount(action: suspend (AuthAccount) -> T): T = accountMutex.withLock {
        val account = preferences.account ?: throw IllegalStateException("Account should not be null")
        val androidAccount = account.toAndroidAccount()
        requireToken(authToken.refreshToken(androidAccount))
        val refreshedAccount = deviceRegistration.ensure(account, allowCheckIn = false)
        action(refreshedAccount)
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