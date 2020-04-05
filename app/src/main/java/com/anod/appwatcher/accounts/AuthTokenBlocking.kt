package com.anod.appwatcher.accounts

import android.accounts.*
import android.app.Activity
import android.content.Context
import android.os.Bundle
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AuthTokenBlocking(context: ApplicationContext) {
    constructor(context: Context) : this(ApplicationContext(context))

    private val accountManager: AccountManager = AccountManager.get(context.actual)

    suspend fun retrieve(activity: Activity?, acc: Account): String = withContext(Dispatchers.IO) {
        var token = ""
        try {
            token = getAuthToken(activity, acc)
        } catch (e: Exception) {
            AppLog.e(e)
        }

        if (token.isNotEmpty()) {
            accountManager.invalidateAuthToken(ACCOUNT_TYPE, token)
        }

        try {
            token = getAuthToken(activity, acc)
        } catch (e: Exception) {
            AppLog.e(e)
        }
        return@withContext token
    }

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    private fun getAuthToken(activity: Activity?, acc: Account?): String {
        if (acc == null) {
            return ""
        }

        if (activity == null) {
            val future = accountManager.getAuthToken(acc, AUTH_TOKEN_TYPE, null, false, null, null)
            return future.result.getString(AccountManager.KEY_AUTHTOKEN) ?: ""
        }

        val future: AccountManagerFuture<Bundle> = accountManager.getAuthToken(
                acc, AUTH_TOKEN_TYPE, null, activity, null, null
        )
        return future.result.getString(AccountManager.KEY_AUTHTOKEN) ?: ""
    }

    companion object {
        private const val AUTH_TOKEN_TYPE = "androidmarket"
        const val ACCOUNT_TYPE = "com.google"
    }
}
