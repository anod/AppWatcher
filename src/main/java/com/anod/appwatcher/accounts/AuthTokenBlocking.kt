package com.anod.appwatcher.accounts

import android.accounts.*
import android.accounts.AccountManager
import android.app.Activity
import android.os.Bundle
import info.anodsplace.android.log.AppLog
import info.anodsplace.appwatcher.framework.ApplicationContext
import java.io.IOException

class AuthTokenBlocking(context: ApplicationContext) {
    private val accountManager: AccountManager = AccountManager.get(context.actual)

    fun request(activity: Activity?, acc: Account): String {
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
        return token
    }

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    private fun getAuthToken(activity: Activity?, acc: Account?): String {
        if (acc == null) {
            return ""
        }

        if (activity == null)
        {
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
