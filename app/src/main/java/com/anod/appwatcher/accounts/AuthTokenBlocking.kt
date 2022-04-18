package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Intent
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

class AuthTokenStartIntent(val intent: Intent) : RuntimeException("getAuthToken finished with intent: $intent")

class AuthTokenBlocking(context: ApplicationContext) {

    companion object {
        private const val AUTH_TOKEN_TYPE = "androidmarket"
        const val ACCOUNT_TYPE = "com.google"
        private val expiration = TimeUnit.MINUTES.toMillis(5L)
    }

    val isFresh: Boolean
        get() = token.isNotEmpty() && lastUpdated > 0 && (System.currentTimeMillis() - lastUpdated) < expiration

    val tokenState = MutableStateFlow("")
    val tokenAvailable = tokenState.filter { it.isNotEmpty() }
    val token: String
        get() = tokenState.value

    private val accountManager: AccountManager = AccountManager.get(context.actual)
    private var lastUpdated = 0L

    suspend fun refreshToken(account: Account): Boolean = withContext(Dispatchers.Main) {
        tokenState.value = retrieve(account)
        if (tokenState.value.isEmpty()) {
            return@withContext false
        }
        lastUpdated = System.currentTimeMillis()
        return@withContext true
    }

    private suspend fun retrieve(acc: Account): String = withContext(Dispatchers.IO) {
        var token = ""
        try {
            token = getAuthToken(acc)
        } catch (e: Exception) {
            if (e is AuthTokenStartIntent) {
                throw e
            } else {
                AppLog.e(e)
            }
        }

        if (token.isNotEmpty()) {
            accountManager.invalidateAuthToken(ACCOUNT_TYPE, token)
        }

        try {
            token = getAuthToken(acc)
        } catch (e: Exception) {
            if (e is AuthTokenStartIntent) {
                throw e
            } else {
                AppLog.e(e)
            }
        }
        return@withContext token
    }

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    private fun getAuthToken(acc: Account): String {
        val bundle = accountManager.getAuthToken(acc, AUTH_TOKEN_TYPE, null, false, null, null).result
        val token = bundle.getString(AccountManager.KEY_AUTHTOKEN) ?: ""

        if (token.isEmpty()) {
            bundle.getParcelable<Intent?>(AccountManager.KEY_INTENT)?.let {
                throw AuthTokenStartIntent(it)
            }
        }

        return token
    }
}