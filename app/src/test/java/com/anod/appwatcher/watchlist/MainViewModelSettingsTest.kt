package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.DeviceRegistration
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.preferences.TestNotificationManager
import com.anod.appwatcher.sync.SyncScheduler
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** How the main screen reacts to the settings: the notification permission result and the watch list preferences. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class MainViewModelSettingsTest {
    private class WatchListSetting(
        val name: String,
        val change: (Preferences) -> Unit,
        val shown: (WatchListPreferences) -> Any,
        val expected: Any
    )

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notifications = TestNotificationManager()
    private val preferences = Preferences(app, notifications, appScope, DiagnosticAllowlist(emptySet()))
    private val viewModelStore = ViewModelStore()
    private val actions = CopyOnWriteArrayList<MainViewAction>()
    private val diamond = "M50,0 L100,50 L50,100 L0,50 Z"
    private var collector: Job? = null
    private lateinit var database: AppsDatabase
    private lateinit var viewModel: MainViewModel

    private val watchListSettings = listOf(
        WatchListSetting("default filter", { it.defaultMainFilterId = Filters.UPDATABLE }, { it.defaultFilterId }, Filters.UPDATABLE),
        WatchListSetting("on-device apps", { it.showOnDevice = true }, { it.showOnDeviceApps }, true),
        WatchListSetting("recently installed apps", { it.showRecent = false }, { it.showRecentlyInstalledApps }, false),
        WatchListSetting("recently discovered apps", { it.showRecentlyDiscovered = false }, { it.showRecentlyDiscoveredApps }, false),
        WatchListSetting("pull to refresh", { it.enablePullToRefresh = false }, { it.enablePullToRefresh }, false),
        WatchListSetting("icon shape", { it.iconShape = diamond }, { it.iconShape }, diamond)
    )

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        database = Room.inMemoryDatabaseBuilder(app, AppsDatabase::class.java).allowMainThreadQueries().build()
        runBlocking {
            assertTrue(
                preferences.saveAccount(
                    AuthAccount("account@example.com", AuthTokenBlocking.ACCOUNT_TYPE, "4d2", "checkin", "config"),
                    deviceRegistrationPending = false,
                    deviceRegistrationAuthorized = false,
                    deviceConfigRevision = DeviceRegistration.DEVICE_CONFIG_REVISION
                )
            )
        }
        startKoin {
            modules(module {
                single<Context> { app }
                single { preferences }
                single { database }
            })
        }
    }

    @After
    fun tearDown() {
        collector?.cancel()
        viewModelStore.clear()
        stopKoin()
        database.close()
        appScope.cancel()
    }

    private fun start() {
        val created = MainViewModel()
        viewModelStore.put("main", created)
        viewModel = created
        collector = CoroutineScope(Dispatchers.Unconfined).launch { created.viewActions.collect { actions += it } }
    }

    private fun idle() = Shadows.shadowOf(Looper.getMainLooper()).idle()

    private fun send(event: MainViewEvent) {
        viewModel.handleEvent(event)
        idle()
    }

    private fun awaitUntil(description: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (!condition()) {
            if (System.nanoTime() > deadline) fail("Timed out waiting for $description")
            idle()
            Thread.sleep(10)
        }
    }

    private fun scheduledSync(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork(SyncScheduler.TAG).get()

    private fun awaitScheduledSync(): WorkInfo {
        awaitUntil("the periodic check to be scheduled") { scheduledSync().isNotEmpty() }
        return scheduledSync().single()
    }

    private fun toasts() = actions.filterIsInstance<MainViewAction.ShowToast>()

    @Test
    fun startsWithTheSavedWatchListSettings() {
        preferences.showOnDevice = true
        preferences.defaultMainFilterId = Filters.INSTALLED
        preferences.enablePullToRefresh = false

        start()

        assertEquals(
            WatchListPreferences(
                defaultFilterId = Filters.INSTALLED,
                showOnDeviceApps = true,
                showRecentlyInstalledApps = true,
                showRecentlyDiscoveredApps = true,
                enablePullToRefresh = false,
                iconShape = preferences.iconShape
            ),
            viewModel.viewStates.value.watchListPreferences
        )
    }

    @Test
    fun everyWatchListSettingReachesTheScreenWhenItChanges() {
        start()

        for (setting in watchListSettings) {
            assertFalse(setting.name, setting.shown(viewModel.viewStates.value.watchListPreferences) == setting.expected)

            setting.change(preferences)

            awaitUntil("${setting.name} to reach the watch list") {
                setting.shown(viewModel.viewStates.value.watchListPreferences) == setting.expected
            }
        }
    }

    @Test
    fun lastUpdateFollowsTheTimeOfTheLastCheck() {
        preferences.lastUpdateTime = 1_000L
        start()
        assertEquals(1_000L, viewModel.viewStates.value.lastUpdate)

        preferences.lastUpdateTime = 1_700_000_000_000L

        awaitUntil("the new last update time") { viewModel.viewStates.value.lastUpdate == 1_700_000_000_000L }
    }

    @Test
    fun warnsAboutDisabledNotificationsOnlyThreeTimes() {
        start()

        repeat(5) { send(MainViewEvent.NotificationPermissionResult(enabled = false)) }

        assertEquals(3, toasts().size)
        assertTrue(toasts().all { it.resId == R.string.notifications_not_enabled && it.length == Toast.LENGTH_LONG })
        assertEquals(3, preferences.notificationDisabledToastCount)
        assertTrue(scheduledSync().isEmpty())
    }

    @Test
    fun countsTheWarningsAlreadyShownInEarlierSessions() {
        preferences.notificationDisabledToastCount = 2
        start()

        repeat(2) { send(MainViewEvent.NotificationPermissionResult(enabled = false)) }

        assertEquals(1, toasts().size)
        assertEquals(3, preferences.notificationDisabledToastCount)
    }

    @Test
    fun grantedNotificationAccessNeverShowsTheWarning() {
        start()

        send(MainViewEvent.NotificationPermissionResult(enabled = true))

        assertTrue(toasts().isEmpty())
        assertEquals(0, preferences.notificationDisabledToastCount)
    }

    @Test
    fun grantingNotificationAccessSchedulesThePeriodicCheckWithTheSavedUpdateSettings() {
        preferences.updatesFrequency = 3600
        preferences.isWifiOnly = true
        preferences.isRequiresCharging = true
        start()

        send(MainViewEvent.NotificationPermissionResult(enabled = true))

        val work = awaitScheduledSync()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(NetworkType.UNMETERED, work.constraints.requiredNetworkType)
        assertTrue(work.constraints.requiresCharging())
        assertEquals(3600_000L, work.periodicityInfo?.repeatIntervalMillis)
    }

    @Test
    fun grantingNotificationAccessDoesNotScheduleAnythingWhenUpdatesAreManual() {
        preferences.updatesFrequency = 0
        start()

        send(MainViewEvent.NotificationPermissionResult(enabled = true))

        assertTrue(scheduledSync().isEmpty())
    }

    @Test
    fun grantResultIsIgnoredWhileTheSystemStillBlocksNotifications() {
        preferences.updatesFrequency = 3600
        notifications.areNotificationsEnabled = false
        start()

        send(MainViewEvent.NotificationPermissionResult(enabled = true))

        assertTrue(scheduledSync().isEmpty())
    }

    @Test
    fun grantingNotificationAccessAgainKeepsTheAlreadyScheduledCheck() {
        preferences.updatesFrequency = 7200
        start()
        send(MainViewEvent.NotificationPermissionResult(enabled = true))
        val scheduled = awaitScheduledSync()

        preferences.updatesFrequency = 3600
        preferences.isWifiOnly = true
        send(MainViewEvent.NotificationPermissionResult(enabled = true))

        val kept = scheduledSync().single()
        assertEquals(scheduled.id, kept.id)
        assertEquals(NetworkType.CONNECTED, kept.constraints.requiredNetworkType)
        assertEquals(7200_000L, kept.periodicityInfo?.repeatIntervalMillis)
    }
}