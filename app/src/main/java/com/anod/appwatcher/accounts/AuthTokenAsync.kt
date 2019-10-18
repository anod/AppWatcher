package com.anod.appwatcher.accounts

import android.accounts.Account
import android.app.Activity
import android.content.Context
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val authScope = CoroutineScope(Dispatchers.Main + Job())

/**
 * @author Alex Gavrishev
 * @date 26/10/2017
 */
class AuthTokenAsync(private val authTokenBlocking: AuthTokenBlocking) {

    constructor(context: ApplicationContext): this(AuthTokenBlocking(context))
    constructor(context: Context): this(ApplicationContext(context))

    fun request(activity: Activity?, account: Account, callback: (token: String) -> Unit) {
        authScope.launch {
            val token = authTokenBlocking.request(activity, account)
            callback(token)
        }
    }
}