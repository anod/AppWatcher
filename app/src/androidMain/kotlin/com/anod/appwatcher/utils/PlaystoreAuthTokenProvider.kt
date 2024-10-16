package com.anod.appwatcher.utils

import com.anod.appwatcher.accounts.AuthTokenBlocking
import finsky.api.DfeAuthTokenProvider

class PlaystoreAuthTokenProvider(private val authTokenBlocking: AuthTokenBlocking) : DfeAuthTokenProvider {
    override val authToken: String
        get() = authTokenBlocking.token
}