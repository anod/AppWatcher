package com.anod.appwatcher.accounts

import android.accounts.Account
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthTokenBlockingTest {

    @Test
    fun routineRefreshDoesNotInvalidateToken() = runBlocking {
        val provider = RecordingTokenProvider("cached-token")
        val authToken = AuthTokenBlocking.create(provider)

        val result = authToken.refreshToken(account())

        assertTrue(result is CheckTokenResult.Success)
        assertFalse((result as CheckTokenResult.Success).invalidated)
        assertEquals(emptyList<String>(), provider.invalidatedTokens)
        assertEquals("cached-token", authToken.token)
    }

    @Test
    fun explicitAuthenticationRecoveryInvalidatesTokenOnce() = runBlocking {
        val provider = RecordingTokenProvider("expired-token", "fresh-token")
        val authToken = AuthTokenBlocking.create(provider)

        val result = authToken.invalidateAndRefreshToken(account())

        assertTrue(result is CheckTokenResult.Success)
        assertTrue((result as CheckTokenResult.Success).invalidated)
        assertEquals(listOf("expired-token"), provider.invalidatedTokens)
        assertEquals("fresh-token", authToken.token)
    }

    @Test
    fun concurrentRecoveryDoesNotInvalidateReplacementToken() = runBlocking {
        val provider = RecordingTokenProvider("expired-token", "fresh-token")
        val authToken = AuthTokenBlocking.create(provider)
        val account = account()
        authToken.refreshToken(account)

        val first = authToken.refreshAfterAuthenticationFailure(account, "expired-token")
        val second = authToken.refreshAfterAuthenticationFailure(account, "expired-token")

        assertTrue((first as CheckTokenResult.Success).invalidated)
        assertFalse((second as CheckTokenResult.Success).invalidated)
        assertEquals(listOf("expired-token"), provider.invalidatedTokens)
        assertEquals("fresh-token", authToken.token)
    }

    @Test
    fun rejectedTokenRemainsAvailableWhileReplacementIsLoading() = runBlocking {
        val provider = BlockingReplacementTokenProvider()
        val authToken = AuthTokenBlocking.create(provider)
        val account = account()
        authToken.refreshToken(account)

        val recovery = async(Dispatchers.Default) {
            authToken.refreshAfterAuthenticationFailure(account, "expired-token")
        }
        try {
            assertTrue(provider.replacementRequested.await(5, TimeUnit.SECONDS))
            assertEquals("expired-token", authToken.tokenFor(authAccount()))
        } finally {
            provider.releaseReplacement.countDown()
        }

        assertTrue((recovery.await() as CheckTokenResult.Success).invalidated)
        assertEquals("fresh-token", authToken.tokenFor(authAccount()))
    }

    @Test
    fun freshTokenIsNotReusedForAnotherAccount() = runBlocking {
        val provider = RecordingTokenProvider("first-token", "second-token")
        val authToken = AuthTokenBlocking.create(provider)

        authToken.refreshToken(account("first@example.com"))
        authToken.checkToken(account("second@example.com"))

        assertEquals("second-token", authToken.token)
        assertEquals(
            listOf("first@example.com", "second@example.com"),
            provider.requestedAccounts
        )
        assertEquals(
            "",
            authToken.tokenFor(
                AuthAccount(
                    name = "first@example.com",
                    type = AuthTokenBlocking.ACCOUNT_TYPE,
                    gfsId = "device",
                    gfsIdToken = "checkin",
                    deviceConfig = "config"
                )
            )
        )
    }

    private fun account(name: String = "account@example.com") = Account(name, AuthTokenBlocking.ACCOUNT_TYPE)

    private fun authAccount() = AuthAccount(
        name = "account@example.com",
        type = AuthTokenBlocking.ACCOUNT_TYPE,
        gfsId = "device",
        gfsIdToken = "checkin",
        deviceConfig = "config"
    )

    private class RecordingTokenProvider(vararg tokens: String) : AccountAuthTokenProvider {
        private val tokens = ArrayDeque(tokens.toList())
        val invalidatedTokens = mutableListOf<String>()
        val requestedAccounts = mutableListOf<String>()

        override fun getAuthToken(account: Account): String {
            requestedAccounts.add(account.name)
            return tokens.removeFirst()
        }

        override fun invalidateAuthToken(token: String) {
            invalidatedTokens.add(token)
        }
    }

    private class BlockingReplacementTokenProvider : AccountAuthTokenProvider {
        val replacementRequested = CountDownLatch(1)
        val releaseReplacement = CountDownLatch(1)
        private var requests = 0

        override fun getAuthToken(account: Account): String {
            requests++
            if (requests == 1) {
                return "expired-token"
            }
            replacementRequested.countDown()
            check(releaseReplacement.await(5, TimeUnit.SECONDS))
            return "fresh-token"
        }

        override fun invalidateAuthToken(token: String) = Unit
    }
}