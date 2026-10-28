package com.anod.appwatcher.accounts

import android.accounts.Account
import android.accounts.OperationCanceledException
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
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

    @Test
    fun cancellationInterruptsBlockedTokenRequest() = runBlocking {
        for (interruptAsOperationCanceled in listOf(false, true)) {
            val provider = BlockingReplacementTokenProvider(interruptAsOperationCanceled)
            val authToken = AuthTokenBlocking.create(provider)
            val account = account()
            authToken.refreshToken(account)

            val recovery = async(Dispatchers.Default) {
                authToken.refreshAfterAuthenticationFailure(account, "expired-token")
            }
            assertTrue(provider.replacementRequested.await(5, TimeUnit.SECONDS))

            val cancelled = withTimeoutOrNull(5_000L) { recovery.cancelAndJoin() }

            assertTrue("Blocked AccountManager wait must be interruptible", cancelled != null)
            assertTrue(provider.interrupted)
            assertTrue(recovery.isCancelled)
            assertTrue(runCatching { recovery.await() }.exceptionOrNull() is CancellationException)
            provider.releaseReplacement.countDown()
            assertTrue(authToken.refreshToken(account) is CheckTokenResult.Success)
        }
    }

    @Test
    fun cancelledSessionWaitKeepsHolderAndLockUsable() = runBlocking {
        val coordinator = PlaySessionCoordinator()
        val holderStarted = CompletableDeferred<Unit>()
        val holder = launch(Dispatchers.Default) {
            coordinator.withSession(owner = "stuck") {
                coordinator.updateStage("auth-token")
                holderStarted.complete(Unit)
                awaitCancellation()
            }
        }
        holderStarted.await()

        var waiterEntered = false
        val waiter = launch(start = CoroutineStart.UNDISPATCHED) {
            coordinator.withSession(owner = "waiter") { waiterEntered = true }
        }
        waiter.cancelAndJoin()

        assertFalse(waiterEntered)
        val description = coordinator.describeHolder()
        assertTrue(description, description.contains("holder=stuck"))
        assertTrue(description, description.contains("stage=auth-token"))
        holder.cancelAndJoin()
        assertEquals("holder=none", coordinator.describeHolder())
        assertEquals(1, coordinator.withSession(owner = "next") { 1 })
    }

    @Test
    fun sessionWaitsForHolderBeforeEntering() = runBlocking {
        val coordinator = PlaySessionCoordinator()
        val release = CompletableDeferred<Unit>()
        val holder = launch(start = CoroutineStart.UNDISPATCHED) {
            coordinator.withSession(owner = "holder") { release.await() }
        }
        var waiterEntered = false
        val waiter = async(start = CoroutineStart.UNDISPATCHED) {
            coordinator.withSession(owner = "waiter") {
                waiterEntered = true
                1
            }
        }

        assertFalse(waiterEntered)
        release.complete(Unit)
        assertEquals(1, waiter.await())
        holder.join()
        assertTrue(waiterEntered)
        assertEquals("holder=none", coordinator.describeHolder())
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

    private class BlockingReplacementTokenProvider(
        private val interruptAsOperationCanceled: Boolean = false
    ) : AccountAuthTokenProvider {
        val replacementRequested = CountDownLatch(1)
        val releaseReplacement = CountDownLatch(1)

        @Volatile
        var interrupted = false
        private var requests = 0

        override fun getAuthToken(account: Account): String {
            requests++
            if (requests == 1) {
                return "expired-token"
            }
            replacementRequested.countDown()
            try {
                check(releaseReplacement.await(5, TimeUnit.SECONDS))
            } catch (e: InterruptedException) {
                interrupted = true
                if (interruptAsOperationCanceled) {
                    throw OperationCanceledException(e)
                }
                throw e
            }
            return "fresh-token"
        }

        override fun invalidateAuthToken(token: String) = Unit
    }
}