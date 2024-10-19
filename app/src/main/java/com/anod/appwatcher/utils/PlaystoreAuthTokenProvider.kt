package com.anod.appwatcher.utils

import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeAuthProvider

class PlaystoreAuthTokenProvider(
    private val authTokenBlocking: AuthTokenBlocking,
    private val preferences: Preferences
) : DfeAuthProvider {
    override val gfsId: String
        get() = preferences.account?.gfsId ?: ""
    override val gfsToken: String
        get() = preferences.account?.deviceCheckInConsistencyToken ?: ""
    override val authToken: String
        get() = authTokenBlocking.token
    override val accountName: String
        get() = preferences.account?.name ?: ""
}