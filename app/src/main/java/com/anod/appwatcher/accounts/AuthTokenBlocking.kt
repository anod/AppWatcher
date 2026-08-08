package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Intent
import info.anodsplace.applog.AppLog
import info.anodsplace.context.ApplicationContext
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AuthTokenStartIntent(val intent: Intent) : RuntimeException("getAuthToken finished with intent: $intent")

sealed interface CheckTokenError {
    class Unknown(val e: Exception) : CheckTokenError
    class RequiresInteraction(val intent: Intent) : CheckTokenError
    object NoToken : CheckTokenError
    object NoAccount : CheckTokenError
}

sealed interface CheckTokenResult {
    class Success(val invalidated: Boolean) : CheckTokenResult
    class Error(val error: CheckTokenError) : CheckTokenResult
}

internal interface AccountAuthTokenProvider {
    fun getAuthToken(account: Account): String
    fun invalidateAuthToken(token: String)
}

private class AccountManagerAuthTokenProvider(private val accountManager: AccountManager) : AccountAuthTokenProvider {
    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    override fun getAuthToken(account: Account): String {
        val bundle = accountManager.getAuthToken(account, AuthTokenBlocking.AUTH_TOKEN_TYPE, null, false, null, null).result
        val token = bundle.getString(AccountManager.KEY_AUTHTOKEN) ?: ""

        if (token.isEmpty()) {
            bundle.getParcelable<Intent?>(AccountManager.KEY_INTENT)?.let {
                throw AuthTokenStartIntent(it)
            }
        }

        return token
    }

    override fun invalidateAuthToken(token: String) {
        accountManager.invalidateAuthToken(AuthTokenBlocking.ACCOUNT_TYPE, token)
    }
}

class AuthTokenBlocking private constructor(private val tokenProvider: AccountAuthTokenProvider) {
    private data class TokenSession(val account: Account, val token: String)

    companion object {
        internal const val AUTH_TOKEN_TYPE = "androidmarket"
        const val ACCOUNT_TYPE = "com.google"
        private val expiration = TimeUnit.MINUTES.toMillis(5L)

        internal fun create(tokenProvider: AccountAuthTokenProvider): AuthTokenBlocking =
            AuthTokenBlocking(tokenProvider)
    }

    constructor(context: ApplicationContext)
        : this(AccountManagerAuthTokenProvider(AccountManager.get(context.actual)))

    val tokenState = MutableStateFlow("")
    val token: String
        get() = tokenSession?.token ?: ""

    private val tokenMutex = Mutex()

    @Volatile
    private var tokenSession: TokenSession? = null
    private var lastUpdated = 0L

    suspend fun checkToken(account: Account?): CheckTokenResult {
        val account = account ?: return CheckTokenResult.Error(CheckTokenError.NoAccount)
        return try {
            tokenMutex.withLock {
                if (isFresh(account)) {
                    CheckTokenResult.Success(invalidated = false)
                } else {
                    refreshTokenLocked(account, invalidate = false)
                }
            }
        } catch (e: AuthTokenStartIntent) {
            CheckTokenResult.Error(CheckTokenError.RequiresInteraction(e.intent))
        } catch (e: Exception) {
            AppLog.e("onResume", e)
            CheckTokenResult.Error(CheckTokenError.Unknown(e))
        }
    }

    suspend fun refreshToken(account: Account): CheckTokenResult = tokenMutex.withLock {
        refreshTokenLocked(account, invalidate = false)
    }

    suspend fun invalidateAndRefreshToken(account: Account): CheckTokenResult = tokenMutex.withLock {
        refreshTokenLocked(account, invalidate = true)
    }

    suspend fun refreshAfterAuthenticationFailure(
        account: Account,
        rejectedToken: String
    ): CheckTokenResult = tokenMutex.withLock {
        val session = tokenSession
        if (
            session != null &&
            isSameAccount(session.account, account) &&
            session.token.isNotEmpty() &&
            session.token != rejectedToken
        ) {
            return@withLock CheckTokenResult.Success(invalidated = false)
        }
        updateToken(account, invalidateRejectedToken(account, rejectedToken))
    }

    private suspend fun refreshTokenLocked(account: Account, invalidate: Boolean): CheckTokenResult {
        if (tokenSession != null && !isSameAccount(tokenSession?.account, account)) {
            tokenSession = null
            tokenState.value = ""
            lastUpdated = 0L
        }
        return updateToken(account, retrieve(account, invalidate))
    }

    private fun updateToken(account: Account, result: Pair<String, Boolean>): CheckTokenResult {
        val (token, invalidated) = result
        tokenState.value = token
        if (tokenState.value.isEmpty()) {
            tokenSession = null
            lastUpdated = 0L
            AppLog.e("Error retrieving token")
            return CheckTokenResult.Error(CheckTokenError.NoToken)
        }
        tokenSession = TokenSession(account, token)
        lastUpdated = System.currentTimeMillis()
        return CheckTokenResult.Success(invalidated = invalidated)
    }

    private fun isFresh(account: Account): Boolean {
        val session = tokenSession ?: return false
        return isSameAccount(session.account, account) &&
            session.token.isNotEmpty() &&
            lastUpdated > 0 &&
            (System.currentTimeMillis() - lastUpdated) < expiration
    }

    private fun isSameAccount(first: Account?, second: Account): Boolean =
        first?.name == second.name && first.type == second.type

    internal fun tokenFor(account: AuthAccount?): String {
        val session = tokenSession ?: return ""
        return if (
            account != null &&
            session.account.name == account.name &&
            session.account.type == account.type
        ) {
            session.token
        } else {
            ""
        }
    }

    private suspend fun retrieve(acc: Account, invalidate: Boolean): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val current = tokenProvider.getAuthToken(acc)
        if (!invalidate || current.isEmpty()) {
            return@withContext Pair(current, false)
        }

        tokenProvider.invalidateAuthToken(current)
        tokenSession = null
        tokenState.value = ""
        lastUpdated = 0L
        return@withContext Pair(tokenProvider.getAuthToken(acc), true)
    }

    private suspend fun invalidateRejectedToken(
        account: Account,
        rejectedToken: String
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        tokenProvider.invalidateAuthToken(rejectedToken)
        Pair(tokenProvider.getAuthToken(account), true)
    }
}