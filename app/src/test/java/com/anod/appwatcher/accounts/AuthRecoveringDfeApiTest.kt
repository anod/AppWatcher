package com.anod.appwatcher.accounts

import android.accounts.Account
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeServerError
import finsky.protos.Details
import info.anodsplace.notification.NotificationManager
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthRecoveringDfeApiTest {
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        preferences = Preferences(
            context = ApplicationProvider.getApplicationContext(),
            notificationManager = NotificationManager.NoOp(),
            appScope = CoroutineScope(Dispatchers.Unconfined)
        )
        preferences.account = AuthAccount(
            name = "account@example.com",
            type = AuthTokenBlocking.ACCOUNT_TYPE,
            gfsId = "device",
            gfsIdToken = "checkin",
            deviceConfig = "config"
        )
    }

    @After
    fun tearDown() {
        preferences.account = null
    }

    @Test
    fun directRequestRefreshesRejectedTokenOnce() = runBlocking {
        val tokenProvider = RecordingTokenProvider(
            "expired-token",
            "fresh-token"
        )
        val authToken = AuthTokenBlocking.create(tokenProvider)
        authToken.refreshToken(Account("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE))
        val delegate = FakeDfeApi().apply {
            detailsFailures.add(DfeServerError("Unauthorized", statusCode = 401))
            detailsResponse = Details.DetailsResponse.getDefaultInstance()
        }
        val dfeApi = AuthRecoveringDfeApi(delegate, authToken, preferences)

        dfeApi.details("details?doc=example")

        assertEquals(2, delegate.detailsCalls)
        assertEquals(listOf("expired-token"), tokenProvider.invalidatedTokens)
        assertEquals("fresh-token", authToken.token)
    }

    private class RecordingTokenProvider(vararg tokens: String) : AccountAuthTokenProvider {
        private val tokens = ArrayDeque(tokens.toList())
        val invalidatedTokens = mutableListOf<String>()

        override fun getAuthToken(account: Account): String = tokens.removeFirst()

        override fun invalidateAuthToken(token: String) {
            invalidatedTokens.add(token)
        }
    }
}