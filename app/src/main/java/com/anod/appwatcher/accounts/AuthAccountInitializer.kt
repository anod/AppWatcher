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
        authToken.refreshToken(account)
        val existingAccount = preferences.account
        if (existingAccount == null || needToRetrieveGfsId(existingAccount, account)) {
            val gfsIdResult = retrieveGsfId()
            val deviceConfigToken = dfeApi.uploadDeviceConfig().uploadDeviceConfigToken
            val authAccount = AuthAccount(account, gfsIdResult, deviceConfigToken)
            preferences.account = authAccount
            return authAccount
        }
        return existingAccount
    }

    suspend fun refresh() {
        val account = preferences.account ?: throw IllegalStateException("Account should not be null")
        val androidAccount = account.toAndroidAccount()
        authToken.refreshToken(androidAccount)
        if (needToRetrieveGfsId(account, androidAccount)) {
            val gfsIdResult = retrieveGsfId()
            val deviceConfigToken = dfeApi.uploadDeviceConfig().uploadDeviceConfigToken
            val authAccount = AuthAccount(androidAccount, gfsIdResult, deviceConfigToken)
            preferences.account = authAccount
        }
    }

    private suspend fun needToRetrieveGfsId(existingAccount: AuthAccount, newAccount: Account): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return false
        }
        return existingAccount.deviceConfig.isEmpty()
                || existingAccount.name != newAccount.name
                || !validate()
    }

    private suspend fun validate(): Boolean {
        return try {
            val app = dfeApi.details("details?doc=com.android.chrome").toDocument()
            val result = app?.appDetails?.packageName == "com.android.chrome"
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