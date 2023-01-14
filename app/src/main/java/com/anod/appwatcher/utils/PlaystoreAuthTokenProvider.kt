package com.anod.appwatcher.utils

import com.anod.appwatcher.accounts.AuthTokenBlocking
import info.anodsplace.playstore.DfeAuthTokenProvider

class PlaystoreAuthTokenProvider(private val authTokenBlocking: AuthTokenBlocking) : DfeAuthTokenProvider {
    override val authToken: String
        get() = authTokenBlocking.token
}