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
import com.anod.appwatcher.CrashlyticsExceptionFilter
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountAuthTokenProvider
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthAccountInitializer
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.DeviceRegistrationNotification
import com.anod.appwatcher.accounts.FakeDfeApi
import com.anod.appwatcher.accounts.PlaySessionCoordinator
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.database.entities.Skipped
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PackageStateCache
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.DfeApi
import finsky.api.DfeServerError
import info.anodsplace.applog.AppLog
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.net.NetworkConnectivity
import info.anodsplace.notification.NotificationManager
import java.io.IOException
import java.net.UnknownHostException
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
import org.koin.dsl.module
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
    private val koin = koinApplication {
        modules(module { single<DfeApi> { dfeApi } })
    }
    private lateinit var preferences: Preferences
    private lateinit var database: AppsDatabase
    private lateinit var updateCheck: UpdateCheck
    private lateinit var initializer: AuthAccountInitializer
    private lateinit var deviceRegistrationNotification: DeviceRegistrationNotification
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
        deviceRegistrationNotification = DeviceRegistrationNotification(
            ApplicationContext(context),
            notificationManager
        )
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
            deviceRegistrationNotification
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
            deviceRegistrationNotification,
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
        assertEquals(List(2) { DeviceRegistrationNotification.NOTIFICATION_ID }, notificationIds)
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
        assertTrue(canceledIds.contains(DeviceRegistrationNotification.NOTIFICATION_ID))
        assertEquals(0, updateCheck.perform(Data.EMPTY))

        assertEquals(1, dfeApi.checkInCalls)
        assertEquals(1, dfeApi.uploadCalls)
        assertEquals("4d2", preferences.account?.gfsId)
        assertEquals(Schedule.STATUS_SUCCESS, database.schedules().load().first().first().result)
        assertTrue(canceledIds.contains(DeviceRegistrationNotification.NOTIFICATION_ID))
        assertTrue(reportedErrors.isEmpty())
    }

    @Test
    fun staleAutomaticInitializationDoesNotClearRequiredRegistrationNotification() = runBlocking {
        assertEquals(-1, updateCheck.perform(Data.EMPTY))

        initializer.initialize(
            Account("old@example.com", AuthTokenBlocking.ACCOUNT_TYPE),
            userInitiated = false
        )

        assertTrue(canceledIds.none { it == DeviceRegistrationNotification.NOTIFICATION_ID })
        assertTrue(preferences.isDeviceRegistrationRequired)
        assertEquals(0, dfeApi.checkInCalls)
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
        assertEquals(false, CrashlyticsExceptionFilter.shouldIgnore(reportedErrors.single()) { false })
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun scheduledNetworkFailureKeepsFailedHistoryWithoutNotificationsAndRecovers() = runBlocking {
        preferences.account = preferences.account!!.copy(gfsId = "existing-id")
        dfeApi.uploadFailure = UnknownHostException("offline")

        repeat(2) {
            assertEquals(-1, updateCheck.perform(Data.EMPTY))
        }

        val schedules = database.schedules().load().first()
        assertEquals(2, schedules.size)
        assertTrue(schedules.all { it.result == Schedule.STATUS_FAILED && it.finish >= it.start && it.notified == 0 })
        assertEquals(-1L, preferences.lastUpdateTime)
        assertTrue(notifications.isEmpty())
        assertEquals(2, reportedErrors.size)
        assertTrue(reportedErrors.all { !CrashlyticsExceptionFilter.shouldIgnore(it) { true } })
        assertTrue(reportedErrors.all { it.message!!.contains("failureKind=expected-transient") })

        dfeApi.uploadFailure = null
        assertEquals(0, updateCheck.perform(Data.EMPTY))
        assertEquals(Schedule.STATUS_SUCCESS, database.schedules().load().first().first().result)
        assertTrue(preferences.lastUpdateTime > 0)
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun scheduledPlayStoreFailuresExhaustChunkRetriesWithLabeledReportsWithoutChangingApps() = runBlocking {
        preferences.account = preferences.account!!.copy(gfsId = "existing-id")
        val app = App.fromLocalPackage(1, "com.example.app", 0, 1, "1", "App", null)
            .copy(status = App.STATUS_NORMAL)
        AppListTable.Queries.insert(app, database)
        val savedApp = database.apps().loadApp(app.appId)

        for (failure in listOf(
            UnknownHostException("offline"),
            DfeServerError("server failure", 500, DfeServerError("display error", null, null))
        )) {
            val callsBefore = dfeApi.bulkDetailsCalls
            dfeApi.bulkDetailsFailure = failure

            assertEquals(-1, updateCheck.perform(Data.EMPTY))

            assertEquals(UpdateCheck.MAX_CHUNK_ATTEMPTS, dfeApi.bulkDetailsCalls - callsBefore)
            val schedule = database.schedules().load().first().first()
            assertEquals(Schedule.STATUS_FAILED, schedule.result)
            assertEquals(0, schedule.notified)
            assertEquals(savedApp, database.apps().loadApp(app.appId))
            assertEquals(-1L, preferences.lastUpdateTime)
            val diagnostic = reportedErrors.last()
            assertTrue(diagnostic.message!!.contains("play-store-update-check"))
            assertEquals(false, CrashlyticsExceptionFilter.shouldIgnore(diagnostic) { true })
            assertTrue(diagnostic.message!!.contains("failureKind=expected-transient"))
            assertTrue(notifications.isEmpty())
        }
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