package com.anod.appwatcher.sync

import android.accounts.Account
import android.app.Application
import android.app.Notification
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountAuthTokenProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.FakeDfeApi
import com.anod.appwatcher.accounts.PlaySessionCoordinator
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.database.entities.Skipped
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PackageStateCache
import com.anod.appwatcher.utils.date.UploadDateParserCache
import info.anodsplace.applog.AppLog
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.notification.NotificationManager
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class UpdateCheckRegistrationTest {
    private val appScope = CoroutineScope(Dispatchers.Unconfined)
    private val notifications = mutableListOf<Notification>()
    private val notificationIds = mutableListOf<Int>()
    private val canceledIds = mutableListOf<Int>()
    private val reportedErrors = mutableListOf<Throwable>()
    private val dfeApi = FakeDfeApi()
    private val koin = koinApplication {}
    private lateinit var preferences: Preferences
    private lateinit var database: AppsDatabase
    private lateinit var updateCheck: UpdateCheck
    private lateinit var initializer: AuthAccountInitializer
    private var previousListener: AppLog.Listener? = null

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notificationManager = object : NotificationManager {
            override val areNotificationsEnabled = true

            override fun notify(notificationId: Int, notification: Notification) {
                notificationIds.add(notificationId)
                notifications.add(notification)
            }

            override fun cancel(notificationId: Int) {
                canceledIds.add(notificationId)
            }
        }
        preferences = Preferences(context, notificationManager, appScope)
        preferences.account = AuthAccount("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE, "", "", "")
        preferences.isWifiOnly = false
        preferences.isDriveSyncEnabled = false
        preferences.lastUpdateTime = -1L
        database = Room.inMemoryDatabaseBuilder(context, AppsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val coordinator = PlaySessionCoordinator()
        val tokenProvider = object : AccountAuthTokenProvider {
            override fun getAuthToken(account: Account) = "token"
            override fun invalidateAuthToken(token: String) = Unit
        }
        initializer = AuthAccountInitializer(
            preferences,
            AuthTokenBlocking.create(tokenProvider),
            dfeApi,
            coordinator,
            onInitializationSucceeded = {
                SyncNotification(ApplicationContext(context), notificationManager).cancelRegistrationRequired()
            }
        )
        updateCheck = UpdateCheck(
            ApplicationContext(context),
            PackageStateCache(context.packageManager),
            notificationManager,
            database,
            preferences,
            NetworkConnectivity(context),
            initializer,
            UploadDateParserCache(),
            coordinator,
            koin.koin
        )
        previousListener = AppLog.instance.listener
        AppLog.instance.listener = object : AppLog.Listener {
            override fun onLogException(tr: Throwable) {
                reportedErrors.add(tr)
            }
        }
    }

    @After
    fun tearDown() {
        AppLog.instance.listener = previousListener
        preferences.account = null
        database.close()
        appScope.cancel()
        koin.close()
    }

    @Test
    fun repeatedScheduledSyncRequestsConfirmationInsteadOfReportingRegistrationFailure() = runBlocking {
        repeat(2) {
            assertEquals(-1, updateCheck.perform(Data.EMPTY))
        }

        val schedules = database.schedules().load().first()
        assertEquals(2, schedules.size)
        assertTrue(schedules.all { it.result == Schedule.STATUS_SKIPPED_DEVICE_REGISTRATION && it.result() is Skipped })
        assertEquals(0, dfeApi.checkInCalls)
        assertEquals("", preferences.account?.gfsId)
        assertTrue("Expected registration state must not be reported: $reportedErrors", reportedErrors.isEmpty())
        assertTrue("Registration must offer an actionable notification", notifications.isNotEmpty())
        assertEquals(List(2) { SyncNotification.REGISTRATION_NOTIFICATION_ID }, notificationIds)
        val notification = notifications.last()
        assertEquals(SyncNotification.AUTHENTICATION_ID, notification.channelId)
        assertEquals(
            ApplicationProvider.getApplicationContext<Context>().getString(R.string.device_registration_required_description),
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertTrue(notification.flags and Notification.FLAG_ONLY_ALERT_ONCE != 0)
        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(AppWatcherActivity::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertEquals(false, intent.getBooleanExtra(AppWatcherActivity.EXTRA_FROM_NOTIFICATION, false))
    }

    @Test
    fun pendingRegistrationAlsoRequiresConfirmationWithoutAnotherCheckIn() = runBlocking {
        assertTrue(preferences.saveDeviceRegistrationPending(true))

        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        assertEquals(Schedule.STATUS_SKIPPED_DEVICE_REGISTRATION, database.schedules().load().first().single().result)
        assertTrue(preferences.isDeviceRegistrationPending)
        assertEquals(0, dfeApi.checkInCalls)
        assertEquals(1, notifications.size)
        assertTrue(reportedErrors.isEmpty())
    }

    @Test
    fun manualSyncAlsoOffersRegistrationResolution() = runBlocking {
        assertEquals(-1, updateCheck.perform(Data.Builder().putBoolean(UpdateCheck.EXTRAS_MANUAL, true).build()))

        val schedule = database.schedules().load().first().single()
        assertEquals(Schedule.REASON_MANUAL, schedule.reason)
        assertEquals(Schedule.STATUS_SKIPPED_DEVICE_REGISTRATION, schedule.result)
        assertEquals(1, notifications.size)
        assertTrue(reportedErrors.isEmpty())
    }

    @Test
    fun explicitConfirmationRegistersOnceAndScheduledSyncResumes() = runBlocking {
        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        initializer.initialize(
            Account("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = true
        )
        assertTrue(canceledIds.contains(SyncNotification.REGISTRATION_NOTIFICATION_ID))
        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(1, dfeApi.checkInCalls)
        assertEquals(1, dfeApi.uploadCalls)
        assertEquals("4d2", preferences.account?.gfsId)
        assertEquals(Schedule.STATUS_SUCCESS, database.schedules().load().first().first().result)
        assertTrue(canceledIds.contains(SyncNotification.REGISTRATION_NOTIFICATION_ID))
        assertTrue(reportedErrors.isEmpty())
    }

    @Test
    fun unexpectedRegistrationUploadFailureStillReportsDiagnostics() = runBlocking {
        preferences.account = preferences.account!!.copy(gfsId = "existing-id")
        dfeApi.uploadFailure = IOException("offline")

        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        assertEquals(Schedule.STATUS_FAILED, database.schedules().load().first().single().result)
        assertTrue(reportedErrors.single() is SyncFailureException)
        assertTrue(reportedErrors.single().message!!.contains("java.io.IOException"))
        assertTrue(notifications.isEmpty())
    }

    @Test
    @Config(sdk = [34])
    fun olderAndroidSyncDoesNotRequireRegistration() = runBlocking {
        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(Schedule.STATUS_SUCCESS, database.schedules().load().first().single().result)
        assertEquals(0, dfeApi.checkInCalls)
        assertTrue(notifications.isEmpty())
        assertTrue(reportedErrors.isEmpty())
    }
}