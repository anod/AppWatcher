package com.anod.appwatcher.accounts

import android.accounts.Account
import android.os.Build
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeApi
import finsky.api.toDocument
import info.anodsplace.applog.AppLog
import java.math.BigInteger

class AuthAccountInitializer(
    private val preferences: Preferences,
    private val authToken: AuthTokenBlocking,
    private val dfeApi: DfeApi
) {
    suspend fun initialize(account: Account): AuthAccount {
        val existingAccount = preferences.account
        val tokenResult = authToken.refreshToken(account)
        val isNewAccount = existingAccount?.name != account.name
        var gfsIdResult: GfsIdResult? = if (isNewAccount) null else existingAccount?.toGfsResult()
        var deviceConfigToken: String? = if (isNewAccount) null else existingAccount?.deviceConfig
        if (needToRetrieveGfsId(existingAccount, account, tokenResult)) {
            gfsIdResult = retrieveGsfId()
            deviceConfigToken = dfeApi.uploadDeviceConfig().uploadDeviceConfigToken
        }
        val authAccount = AuthAccount(
            account,
            gfsIdResult ?: GfsIdResult("", ""),
            deviceConfigToken ?: ""
        )
        preferences.account = authAccount
        return authAccount
    }

    suspend fun refresh() {
        val account = preferences.account ?: throw IllegalStateException("Account should not be null")
        val androidAccount = account.toAndroidAccount()
        val tokenResult = authToken.refreshToken(androidAccount)
        if (needToRetrieveGfsId(account, androidAccount, tokenResult)) {
            val gfsIdResult = retrieveGsfId()
            val deviceConfigToken = dfeApi.uploadDeviceConfig().uploadDeviceConfigToken
            val authAccount = AuthAccount(androidAccount, gfsIdResult, deviceConfigToken)
            preferences.account = authAccount
        }
    }

    private suspend fun needToRetrieveGfsId(existingAccount: AuthAccount?, newAccount: Account, tokenResult: CheckTokenResult): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return false
        }
        val tokenInvalidated = if (tokenResult is CheckTokenResult.Success) tokenResult.invalidated else false
        return existingAccount == null
                || existingAccount.deviceConfig.isEmpty()
                || existingAccount.name != newAccount.name
                || tokenInvalidated
                || canRequest()
    }

    private suspend fun canRequest(): Boolean {
        if (preferences.account == null) {
            return false
        }
        return try {
            val app = dfeApi.details("details?doc=com.anod.appwatcher").toDocument()
            val result = app?.appDetails?.packageName == "com.anod.appwatcher"
            AppLog.d("Auth verification result: $result")
            result
        } catch (e: Exception) {
            AppLog.d("Auth verification failed $e")
            false
        }
    }

    private suspend fun retrieveGsfId(): GfsIdResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return GfsIdResult("", "")
        }
        val response = dfeApi.checkIn()
        if (response.androidId == 0L) {
            throw IllegalStateException("Incorrect androidId")
        }
        val gsfId = BigInteger.valueOf(response.androidId).toString(16)
        return GfsIdResult(gsfId, response.deviceCheckinConsistencyToken ?: "")
    }
}