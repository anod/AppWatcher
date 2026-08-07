package com.anod.appwatcher.utils

import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeAuthData
import finsky.api.DfeAuthProvider

class PlaystoreAuthTokenProvider(private val authTokenBlocking: AuthTokenBlocking, private val preferences: Preferences) : DfeAuthProvider {
    override val gfsId: String
        get() = preferences.account?.gfsId ?: ""
    override val gfsToken: String
        get() = preferences.account?.gfsIdToken ?: ""
    override val authToken: String
        get() = authTokenBlocking.tokenFor(preferences.account)
    override val accountName: String
        get() = preferences.account?.name ?: ""
    override val deviceConfigToken: String
        get() = preferences.account?.deviceConfig ?: ""
    override val authData: DfeAuthData
        get() {
            val account = preferences.account
                ?: return DfeAuthData("", "", "", "", "")
            return DfeAuthData(
                gfsId = account.gfsId,
                gfsToken = account.gfsIdToken,
                authToken = authTokenBlocking.tokenFor(account),
                accountName = account.name,
                deviceConfigToken = account.deviceConfig
            )
        }
}