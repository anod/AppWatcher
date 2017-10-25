package com.anod.appwatcher.accounts

import android.accounts.*
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import com.anod.appwatcher.utils.BackgroundTask
import info.anodsplace.android.log.AppLog
import java.io.IOException

class AuthTokenProvider(context: Context) {
    private val accountManager: AccountManager = AccountManager.get(context)

    fun requestTokenBlocking(activity: Activity?, acc: Account): String {
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

    fun requestToken(activity: Activity?, account: Account, callback: AuthenticateCallback) {
        BackgroundTask(object : BackgroundTask.Worker<Void?, String>(null) {
            override fun run(param: Void?): String {
                return requestTokenBlocking(activity, account)
            }

            override fun finished(result: String) {
                if (result.isEmpty()) {
                    callback.onUnRecoverableException("Cannot retrieve authorization token")
                } else {
                    callback.onAuthTokenAvailable(result)
                }
            }
        }).execute()
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

    interface AuthenticateCallback {
        fun onAuthTokenAvailable(token: String)
        fun onUnRecoverableException(errorMessage: String)
    }

    companion object {
        private const val AUTH_TOKEN_TYPE = "androidmarket"
        const val ACCOUNT_TYPE = "com.google"
    }
}
