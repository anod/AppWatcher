package com.anod.appwatcher.accounts

import android.accounts.*
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import info.anodsplace.android.log.AppLog
import java.io.IOException

class AuthTokenProvider(context: Context) {
    private val mAccountManager: AccountManager = AccountManager.get(context)

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    fun requestTokenBlocking(activity: Activity?, acc: Account): String? {
        var token = getAuthToken(activity, acc)

        if (token != null) {
            mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token)
        }

        token = getAuthToken(activity, acc)
        return token
    }

    fun requestToken(activity: Activity?, acc: Account, callback: AuthenticateCallback) {

        GetTokenTask(activity, acc, this, callback).execute(0)

    }

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    private fun getAuthToken(activity: Activity?, acc: Account?): String? {
        if (acc == null) {
            return null
        }

        if (activity == null)
        {
            val future = mAccountManager.getAuthToken(acc, AUTH_TOKEN_TYPE, null, false, null, null)
            return future.result.getString(AccountManager.KEY_AUTHTOKEN)
        }
        val future: AccountManagerFuture<Bundle> = mAccountManager.getAuthToken(
                acc, AUTH_TOKEN_TYPE, null, activity, null, null
        )
        return future.result.getString(AccountManager.KEY_AUTHTOKEN)
    }

    interface AuthenticateCallback {
        fun onAuthTokenAvailable(token: String)
        fun onUnRecoverableException(errorMessage: String)
    }

    private class GetTokenTask(
            private val mActivity: Activity?,
            private val mAccount: Account,
            private val mAuthTokenProvider: AuthTokenProvider,
            private val mCallback: AuthenticateCallback) : AsyncTask<Int, Void, String?>() {

        override fun doInBackground(vararg params: Int?): String? {
            var token: String? = null
            try {
                token = mAuthTokenProvider.requestTokenBlocking(mActivity, mAccount)
            } catch (e: IOException) {
                AppLog.e(e.message, e)
            } catch (e: OperationCanceledException) {
                AppLog.e(e.message, e)
            } catch (e: AuthenticatorException) {
                AppLog.e(e.message, e)
            }

            return token
        }

        override fun onPostExecute(token: String?) {
            super.onPostExecute(token)
            if (token == null) {
                mCallback.onUnRecoverableException("Cannot retrieve authorization token")
            } else {
                mCallback.onAuthTokenAvailable(token)
            }
        }
    }

    companion object {
        private val AUTH_TOKEN_TYPE = "androidmarket"
        val ACCOUNT_TYPE = "com.google"
    }

}
