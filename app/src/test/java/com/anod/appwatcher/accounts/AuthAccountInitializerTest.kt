package com.anod.appwatcher.accounts

import android.accounts.Account
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.notification.NotificationManager
import java.io.IOException
import java.util.ArrayDeque
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AuthAccountInitializerTest {
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        preferences = Preferences(
            context = ApplicationProvider.getApplicationContext(),
            notificationManager = NotificationManager.NoOp(),
            appScope = CoroutineScope(Dispatchers.Unconfined)
        )
        runBlocking {
            preferences.saveAccount(
                completeAccount(),
                deviceRegistrationPending = null,
                deviceRegistrationAuthorized = null,
                deviceConfigRevision = DeviceRegistration.DEVICE_CONFIG_REVISION
            )
        }
    }

    @After
    fun tearDown() {
        preferences.account = null
    }

    @Test
    fun routineRefreshDoesNotProbeArbitraryValidationPackage() = runBlocking {
        val dfeApi = FakeDfeApi().apply {
            detailsFailures.add(IOException("offline"))
        }
        val tokenProvider = RecordingTokenProvider("cached-token")
        val initializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(tokenProvider),
            dfeApi,
            PlaySessionCoordinator()
        )

        initializer.refresh()

        assertEquals(0, dfeApi.checkInCalls)
        assertEquals(0, dfeApi.detailsCalls)
        assertEquals(emptyList<String>(), tokenProvider.invalidatedTokens)
        assertEquals(completeAccount(), preferences.account)
    }

    @Test
    fun registrationAuthenticationFailureRetriesUploadWithoutAnotherCheckin() = runBlocking {
        preferences.account = AuthAccount(
            name = "new@example.com",
            type = AuthTokenBlocking.ACCOUNT_TYPE,
            gfsId = "",
            gfsIdToken = "",
            deviceConfig = ""
        )
        val dfeApi = FakeDfeApi().apply {
            uploadFailures.add(finsky.api.DfeServerError("Unauthorized", statusCode = 401, cause = null))
        }
        val tokenProvider = RecordingTokenProvider("cached-token", "fresh-token")
        val authToken = AuthTokenBlocking.create(tokenProvider)
        val initializer = AuthAccountInitializer(
            preferences,
            authToken,
            AuthRecoveringDfeApi(dfeApi, authToken, preferences),
            PlaySessionCoordinator()
        )

        initializer.initialize(
            Account("new@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = true
        )

        assertEquals(1, dfeApi.checkInCalls)
        assertEquals(2, dfeApi.uploadCalls)
        assertEquals(listOf("cached-token"), tokenProvider.invalidatedTokens)
        assertEquals("4d2", preferences.account?.gfsId)
        assertEquals("config-token", preferences.account?.deviceConfig)
    }

    @Test
    fun interactiveTokenFlowRetainsOneShotRegistrationAuthorization() = runBlocking {
        preferences.account = null
        var tokenRequests = 0
        val tokenProvider = object : AccountAuthTokenProvider {
            override fun getAuthToken(account: Account): String {
                tokenRequests++
                if (tokenRequests == 1) {
                    throw AuthTokenStartIntent(Intent())
                }
                return "token"
            }

            override fun invalidateAuthToken(token: String) = Unit
        }
        val dfeApi = FakeDfeApi()
        val initializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(tokenProvider),
            dfeApi,
            PlaySessionCoordinator()
        )
        val account = Account("new@example.com", AuthTokenBlocking.ACCOUNT_TYPE)

        try {
            initializer.initialize(account, userInitiated = true)
            throw AssertionError("Expected interactive authentication")
        } catch (_: AuthTokenStartIntent) {
        }

        assertTrue(preferences.isDeviceRegistrationAuthorized)
        assertEquals(0, dfeApi.checkInCalls)

        initializer.initialize(account, userInitiated = false)

        assertFalse(preferences.isDeviceRegistrationAuthorized)
        assertEquals(1, dfeApi.checkInCalls)
    }

    @Test
    fun interactiveAccountSwitchFailsFastWhileSyncSessionIsActive() = runBlocking {
        val dfeApi = FakeDfeApi()
        val playSessionCoordinator = PlaySessionCoordinator()
        val initializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(RecordingTokenProvider("token-a", "token-b")),
            dfeApi,
            playSessionCoordinator
        )
        val actionStarted = CompletableDeferred<Unit>()
        val finishAction = CompletableDeferred<Unit>()

        val session = async {
            playSessionCoordinator.withSession {
                actionStarted.complete(Unit)
                finishAction.await()
            }
        }
        actionStarted.await()

        try {
            initializer.initialize(
                Account("b@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
                userInitiated = true
            )
            throw AssertionError("Expected active synchronization failure")
        } catch (_: AccountSessionBusyException) {
        }
        assertEquals("account@example.com", preferences.account?.name)

        finishAction.complete(Unit)
        session.await()
        initializer.initialize(
            Account("b@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = true
        )

        assertEquals("b@example.com", preferences.account?.name)
    }

    @Test
    fun accountSwitchesReuseDeviceIdentityAcrossInitializers() = runBlocking {
        val playSessionCoordinator = PlaySessionCoordinator()
        val accountBApi = FakeDfeApi()
        val accountBInitializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(RecordingTokenProvider("token-b")),
            accountBApi,
            playSessionCoordinator
        )

        accountBInitializer.initialize(
            Account("b@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = true
        )

        assertEquals(0, accountBApi.checkInCalls)
        assertEquals("existing-id", preferences.account?.gfsId)

        val accountAApi = FakeDfeApi()
        val recreatedInitializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(RecordingTokenProvider("token-a")),
            accountAApi,
            playSessionCoordinator
        )

        recreatedInitializer.initialize(
            Account("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = true
        )

        assertEquals(0, accountAApi.checkInCalls)
        assertEquals("existing-id", preferences.account?.gfsId)
    }

    @Test
    fun staleAutomaticInitializationDoesNotRestorePreviousAccount() = runBlocking {
        val dfeApi = FakeDfeApi()
        val tokenProvider = RecordingTokenProvider("token-b")
        val initializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(tokenProvider),
            dfeApi,
            PlaySessionCoordinator()
        )

        initializer.initialize(
            Account("b@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = true
        )

        val result = initializer.initialize(
            Account("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = false
        )

        assertEquals("b@example.com", result.name)
        assertEquals("b@example.com", preferences.account?.name)
        assertEquals(1, tokenProvider.requestedAccounts.size)
    }

    private fun completeAccount() = AuthAccount(
        name = "account@example.com",
        type = AuthTokenBlocking.ACCOUNT_TYPE,
        gfsId = "existing-id",
        gfsIdToken = "existing-checkin",
        deviceConfig = "existing-config"
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
}