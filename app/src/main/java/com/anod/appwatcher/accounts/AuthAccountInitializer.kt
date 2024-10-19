package com.anod.appwatcher.accounts

import android.accounts.Account
import android.os.Build
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeApi
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
            val authAccount = AuthAccount(account, gfsIdResult)
            preferences.account = authAccount
            return authAccount
        }
        return existingAccount
    }

    private fun needToRetrieveGfsId(existingAccount: AuthAccount, newAccount: Account): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return false
        }
        return existingAccount.gfsId.isEmpty() || existingAccount.name != newAccount.name
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