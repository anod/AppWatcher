package com.anod.appwatcher.accounts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.preferences.Preferences
import finsky.api.DfeDeviceIdentity
import info.anodsplace.notification.NotificationManager
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
class DeviceRegistrationTest {
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        preferences = Preferences(
            context = ApplicationProvider.getApplicationContext(),
            notificationManager = NotificationManager.NoOp(),
            appScope = CoroutineScope(Dispatchers.Unconfined)
        )
        preferences.account = null
    }

    @After
    fun tearDown() {
        preferences.account = null
    }

    @Test
    fun completeIdentityIsReused() = runBlocking {
        val dfeApi = FakeDfeApi()
        val account = account(gfsId = "existing-id", gfsToken = "existing-checkin", deviceConfig = "existing-config")
        assertTrue(
            preferences.saveAccount(
                account,
                deviceRegistrationPending = null,
                deviceRegistrationAuthorized = null,
                deviceConfigRevision = DeviceRegistration.DEVICE_CONFIG_REVISION
            )
        )

        val result = DeviceRegistration(preferences, dfeApi, sdkInt = 35)
            .ensure(account, allowCheckIn = false)

        assertEquals(account, result)
        assertEquals(0, dfeApi.checkInCalls)
        assertEquals(0, dfeApi.uploadCalls)
    }

    @Test
    fun legacyConfigTokenIsReboundWithoutNewCheckin() = runBlocking {
        val dfeApi = FakeDfeApi()
        val account = account(gfsId = "existing-id", gfsToken = "existing-checkin", deviceConfig = "legacy-config")
        assertTrue(
            preferences.saveAccount(
                account,
                deviceRegistrationPending = null,
                deviceRegistrationAuthorized = null,
                deviceConfigRevision = 0
            )
        )

        val result = DeviceRegistration(preferences, dfeApi, sdkInt = 35)
            .ensure(account, allowCheckIn = false)

        assertEquals(0, dfeApi.checkInCalls)
        assertEquals(1, dfeApi.uploadCalls)
        assertEquals(
            DfeDeviceIdentity("existing-id", "existing-checkin", ""),
            dfeApi.uploadIdentities.single()
        )
        assertEquals("config-token", result.deviceConfig)
        assertEquals(DeviceRegistration.DEVICE_CONFIG_REVISION, preferences.deviceConfigRevision)
    }

    @Test
    fun newIdentityIsUsedForDeviceConfigUpload() = runBlocking {
        val dfeApi = FakeDfeApi()

        val result = DeviceRegistration(preferences, dfeApi, sdkInt = 35)
            .ensure(account(), allowCheckIn = true)

        assertEquals("4d2", result.gfsId)
        assertEquals("checkin-token", result.gfsIdToken)
        assertEquals("config-token", result.deviceConfig)
        assertEquals(
            listOf(DfeDeviceIdentity("4d2", "checkin-token", "")),
            dfeApi.uploadIdentities
        )
        assertEquals(result, preferences.account)
    }

    @Test
    fun failedConfigUploadKeepsPendingIdentityForRetry() = runBlocking {
        val dfeApi = FakeDfeApi().apply {
            uploadFailure = IOException("offline")
        }
        val registration = DeviceRegistration(preferences, dfeApi, sdkInt = 35)

        try {
            registration.ensure(account(), allowCheckIn = true)
            throw AssertionError("Expected upload failure")
        } catch (_: IOException) {
        }

        val pendingAccount = preferences.account
        assertEquals("4d2", pendingAccount?.gfsId)
        assertEquals("checkin-token", pendingAccount?.gfsIdToken)
        assertTrue(pendingAccount?.deviceConfig.isNullOrEmpty())
        assertEquals(false, preferences.isDeviceRegistrationPending)
        assertEquals(1, dfeApi.checkInCalls)

        dfeApi.uploadFailure = null
        val result = registration.ensure(pendingAccount!!, allowCheckIn = false)

        assertEquals("config-token", result.deviceConfig)
        assertEquals(1, dfeApi.checkInCalls)
        assertEquals(2, dfeApi.uploadCalls)
    }

    @Test
    fun sessionCoordinatorSerializesConcurrentRegistration() = runBlocking {
        val dfeApi = FakeDfeApi().apply {
            beforeCheckIn = { delay(50) }
        }
        val registration = DeviceRegistration(preferences, dfeApi, sdkInt = 35)
        val playSessionCoordinator = PlaySessionCoordinator()
        val account = account()

        val results = coroutineScope {
            awaitAll(
                async {
                    playSessionCoordinator.withSession {
                        registration.ensure(account, allowCheckIn = true)
                    }
                },
                async {
                    playSessionCoordinator.withSession {
                        registration.ensure(account, allowCheckIn = true)
                    }
                }
            )
        }

        assertEquals(1, dfeApi.checkInCalls)
        assertEquals(1, dfeApi.uploadCalls)
        assertEquals(results[0], results[1])
    }

    @Test
    fun cancelledCallerStillPersistsReturnedIdentity() = runBlocking {
        val checkInStarted = CompletableDeferred<Unit>()
        val releaseCheckIn = CompletableDeferred<Unit>()
        val dfeApi = FakeDfeApi().apply {
            beforeCheckIn = {
                checkInStarted.complete(Unit)
                releaseCheckIn.await()
            }
        }
        val registration = DeviceRegistration(preferences, dfeApi, sdkInt = 35)
        val registrationJob = launch {
            registration.ensure(account(), allowCheckIn = true)
        }
        checkInStarted.await()

        registrationJob.cancel()
        releaseCheckIn.complete(Unit)
        registrationJob.join()

        assertEquals("4d2", preferences.account?.gfsId)
        assertFalse(preferences.isDeviceRegistrationPending)
        val result = registration.ensure(preferences.account!!, allowCheckIn = false)
        assertEquals("4d2", result.gfsId)
        assertEquals(1, dfeApi.checkInCalls)
    }

    @Test
    fun ambiguousCheckinFailureIsNotRetriedAutomatically() = runBlocking {
        val dfeApi = FakeDfeApi().apply {
            checkInFailures.add(IOException("response lost"))
        }
        val account = account()

        try {
            DeviceRegistration(preferences, dfeApi, sdkInt = 35)
                .ensure(account, allowCheckIn = true)
            throw AssertionError("Expected check-in failure")
        } catch (_: IOException) {
        }

        assertEquals(true, preferences.isDeviceRegistrationPending)
        assertEquals(1, dfeApi.checkInCalls)

        try {
            DeviceRegistration(preferences, dfeApi, sdkInt = 35)
                .ensure(account, allowCheckIn = false)
            throw AssertionError("Expected pending registration failure")
        } catch (_: DeviceRegistrationPendingException) {
        }

        assertEquals(1, dfeApi.checkInCalls)

        val result = DeviceRegistration(preferences, dfeApi, sdkInt = 35)
            .ensure(account, allowCheckIn = true)

        assertEquals("4d2", result.gfsId)
        assertEquals(2, dfeApi.checkInCalls)
    }

    @Test
    fun routineRegistrationRequiresExplicitConfirmation() = runBlocking {
        val dfeApi = FakeDfeApi()

        try {
            DeviceRegistration(preferences, dfeApi, sdkInt = 35)
                .ensure(account(), allowCheckIn = false)
            throw AssertionError("Expected registration requirement")
        } catch (_: DeviceRegistrationRequiredException) {
        }

        assertEquals(0, dfeApi.checkInCalls)
        assertEquals(false, preferences.isDeviceRegistrationPending)
    }

    @Test
    fun sameDeviceUpgradeMovesRegistrationOutOfBackupPreferences() {
        val legacyPreferences = testPreferences("upgrade-legacy")
        val devicePreferences = testPreferences("upgrade-device")
        legacyPreferences.edit()
            .putString("account_name", "user@example.com")
            .putString("account_type", "com.google")
            .putString("gfs_id", "123")
            .putString("gfs_token", "token")
            .putString("device_config", "config")
            .putBoolean("device_registration_pending", true)
            .putBoolean("device_registration_authorized", true)
            .putInt("device_config_revision", 1)
            .commit()

        Preferences.migrateDeviceRegistrationPreferences(
            legacyPreferences,
            devicePreferences,
            isSameDeviceUpgrade = true,
            wasRestored = false
        )

        assertEquals("user@example.com", devicePreferences.getString("account_name", null))
        assertEquals("123", devicePreferences.getString("gfs_id", null))
        assertTrue(devicePreferences.getBoolean("device_registration_pending", false))
        assertFalse(legacyPreferences.contains("account_name"))
        assertFalse(legacyPreferences.contains("gfs_id"))
    }

    @Test
    fun restoredRegistrationIsDiscarded() {
        val legacyPreferences = testPreferences("restore-legacy")
        val devicePreferences = testPreferences("restore-device")
        legacyPreferences.edit()
            .putString("account_name", "user@example.com")
            .putString("account_type", "com.google")
            .putString("gfs_id", "123")
            .putString("gfs_token", "token")
            .putString("device_config", "config")
            .commit()

        Preferences.migrateDeviceRegistrationPreferences(
            legacyPreferences,
            devicePreferences,
            isSameDeviceUpgrade = true,
            wasRestored = true
        )

        assertFalse(devicePreferences.contains("account_name"))
        assertFalse(devicePreferences.contains("gfs_id"))
        assertFalse(legacyPreferences.contains("account_name"))
        assertFalse(legacyPreferences.contains("gfs_id"))
    }

    @Test
    fun signedLegacyDeviceIdIsNormalizedToUnsignedHex() {
        assertEquals(
            "ffffffffffffffff",
            Preferences.normalizeDeviceId("-1")
        )
        assertEquals("", Preferences.normalizeDeviceId("not-a-device-id"))
        assertEquals("", Preferences.normalizeDeviceId("0"))
        assertEquals("", Preferences.normalizeDeviceId("0000000000000000"))
    }

    @Test
    fun incompleteAndroid15AccountRequiresExplicitRegistration() {
        preferences.account = account()
        assertTrue(preferences.isDeviceRegistrationRequired)

        preferences.account = account(gfsId = "4d2")
        assertFalse(preferences.isDeviceRegistrationRequired)
    }

    private fun testPreferences(name: String) =
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences(name, Context.MODE_PRIVATE)
            .also { it.edit().clear().commit() }

    private fun account(
        gfsId: String = "",
        gfsToken: String = "",
        deviceConfig: String = ""
    ) = AuthAccount(
        name = "account@example.com",
        type = AuthTokenBlocking.ACCOUNT_TYPE,
        gfsId = gfsId,
        gfsIdToken = gfsToken,
        deviceConfig = deviceConfig
    )
}