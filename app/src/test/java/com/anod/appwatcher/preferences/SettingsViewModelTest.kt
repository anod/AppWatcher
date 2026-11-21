package com.anod.appwatcher.preferences

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.text.format.DateUtils
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthAccount
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.SchedulesTable
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.diagnostics.DiagnosticAccounts
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.sync.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.CheckablePreferenceItem
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.SingleValuePreferenceItem
import info.anodsplace.playservices.GooglePlayServices
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.StringWriter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class SettingsViewModelTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val recorderJob = SupervisorJob()
    private val notifications = TestNotificationManager()
    private val preferences = Preferences(
        app, notifications, appScope,
        DiagnosticAllowlist(setOf(DiagnosticAccounts.hash("diagnostic-account")))
    )
    private val originalLogger = AppLog.logger
    private val viewModelStore = ViewModelStore()
    private val actions = CopyOnWriteArrayList<SettingsViewAction>()
    private val backupUri = Uri.parse("content://com.anod.appwatcher.test/backup.json")
    private var collector: Job? = null
    private lateinit var database: AppsDatabase
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        database = Room.inMemoryDatabaseBuilder(app, AppsDatabase::class.java).allowMainThreadQueries().build()
        val recorder = DiagnosticRecorder(app, preferences, CoroutineScope(recorderJob + Dispatchers.IO), workInfos = { emptyList() })
        startKoin { modules(settingsTestModule(app, appScope, notifications, preferences, recorder, database)) }
    }

    @After
    fun tearDown() {
        collector?.cancel()
        viewModelStore.clear()
        stopKoin()
        database.close()
        runBlocking { recorderJob.cancelAndJoin() }
        appScope.cancel()
        AppLog.logger = originalLogger
    }

    private fun start() {
        val created = SettingsViewModel()
        viewModelStore.put("settings", created)
        viewModel = created
        collector = CoroutineScope(Dispatchers.Unconfined).launch { created.viewActions.collect { actions += it } }
    }

    private fun idle() = Shadows.shadowOf(Looper.getMainLooper()).idle()

    private fun send(event: SettingsViewEvent) {
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

    private fun List<PreferenceItem>.keys() = filterIsInstance<ActionablePreferenceItem>().map { it.key }

    private fun item(key: String) = viewModel.viewStates.value.items.filterIsInstance<ActionablePreferenceItem>().single { it.key == key }

    private fun checked(key: String) = (item(key) as CheckablePreferenceItem).checked

    private fun value(key: String) = (item(key) as SingleValuePreferenceItem).value

    private fun scheduledSync(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork(SyncScheduler.TAG).get()

    private fun scheduledDriveUploads(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork("GDriveUpload").get()

    private fun driveSignInResult(resultCode: Int, data: Intent?) = SettingsViewEvent.GDriveActivityResult(ActivityResult(resultCode, data))

    private fun results(action: (SettingsViewAction) -> Int?) = actions.mapNotNull(action)

    private fun exportResults() = results { (it as? SettingsViewAction.ExportResult)?.result }

    private fun importResults() = results { (it as? SettingsViewAction.ImportResult)?.result }

    private fun schedule(start: Long) = Schedule(
        id = -1L, start = start, finish = start + 1_000, reason = Schedule.REASON_SCHEDULE, result = Schedule.STATUS_SUCCESS,
        checked = 0, found = 0, unavailable = 0, notified = 0
    )

    @Test
    fun startsWithTheSettingsAndTheNotificationAccessOfTheDevice() {
        notifications.areNotificationsEnabled = false

        start()

        val state = viewModel.viewStates.value
        assertFalse(state.areNotificationsEnabled)
        assertFalse(state.isProgressVisible)
        assertEquals(preferenceItems(preferences, false, GooglePlayServices(app), app, DiagnosticStorageState()).keys(), state.items.keys())
        assertFalse(item("update_frequency").enabled)
    }

    @Test
    fun theScreenStillOpensWhenTheDiagnosticsLogCannotBeRead() {
        runBlocking { recorderJob.cancelAndJoin() }
        val uncaught = CopyOnWriteArrayList<Throwable>()
        val thread = Thread.currentThread()
        val previous = thread.uncaughtExceptionHandler
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, error -> uncaught += error }

        try {
            start()
            idle()
        } finally {
            thread.uncaughtExceptionHandler = previous
        }

        assertEquals(emptyList<Throwable>(), uncaught.toList())
        assertEquals(preferenceItems(preferences, false, GooglePlayServices(app), app, DiagnosticStorageState()).keys(), viewModel.viewStates.value.items.keys())
    }

    @Test
    fun settingsChangedElsewhereRefreshTheVisibleItems() {
        start()
        assertFalse(checked("show-on-device"))

        preferences.showOnDevice = true

        awaitUntil("the list to show the new value") { checked("show-on-device") }
    }

    @Test
    fun changingTheUpdatePolicySavesItAndSchedulesThePeriodicCheckWithMatchingConstraints() {
        start()

        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 3600, isWifiOnly = true, isRequiresCharging = true))

        assertEquals(3600, preferences.updatesFrequency)
        assertTrue(preferences.isWifiOnly)
        assertTrue(preferences.isRequiresCharging)
        val work = scheduledSync().single()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(NetworkType.UNMETERED, work.constraints.requiredNetworkType)
        assertTrue(work.constraints.requiresCharging())
        assertEquals(3600_000L, work.periodicityInfo?.repeatIntervalMillis)
    }

    @Test
    fun changingThePolicyAgainUpdatesTheSameScheduledCheck() {
        start()
        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 3600, isWifiOnly = true, isRequiresCharging = true))
        val first = scheduledSync().single()

        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 21600, isWifiOnly = false, isRequiresCharging = false))

        val updated = scheduledSync().single()
        assertEquals(first.id, updated.id)
        assertEquals(NetworkType.CONNECTED, updated.constraints.requiredNetworkType)
        assertFalse(updated.constraints.requiresCharging())
        assertEquals(21600_000L, updated.periodicityInfo?.repeatIntervalMillis)
    }

    @Test
    fun choosingManualUpdatesCancelsTheScheduledCheckAndDisablesTheConstraints() {
        start()
        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 3600, isWifiOnly = false, isRequiresCharging = false))

        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 0, isWifiOnly = false, isRequiresCharging = false))

        assertEquals(0, preferences.updatesFrequency)
        assertEquals(WorkInfo.State.CANCELLED, scheduledSync().single().state)
        awaitUntil("the list to show manual updates") { value("update_frequency") == "0" }
        assertFalse(item("wifi_only").enabled)
        assertFalse(item("requires-charging").enabled)
    }

    @Test
    fun savedUpdatePolicyIsNotScheduledWithoutNotificationAccess() {
        notifications.areNotificationsEnabled = false
        start()

        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 3600, isWifiOnly = false, isRequiresCharging = false))

        assertEquals(3600, preferences.updatesFrequency)
        assertTrue(scheduledSync().isEmpty())
    }

    @Test
    fun revokingNotificationAccessCancelsTheScheduledCheckOnTheNextPolicyChange() {
        start()
        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 3600, isWifiOnly = false, isRequiresCharging = false))
        assertEquals(WorkInfo.State.ENQUEUED, scheduledSync().single().state)

        notifications.areNotificationsEnabled = false
        send(SettingsViewEvent.ChangeUpdatePolicy(frequency = 3600, isWifiOnly = true, isRequiresCharging = false))

        assertEquals(WorkInfo.State.CANCELLED, scheduledSync().single().state)
    }

    @Test
    fun everyThemeChangeRestartsTheAppAndKeepingTheThemeDoesNot() {
        start()

        for (from in 0..4) {
            for (to in 0..4) {
                preferences.themeIndex = from
                actions.clear()

                send(SettingsViewEvent.UpdateTheme(to))

                assertEquals("theme $from -> $to", to, preferences.themeIndex)
                assertEquals("restarts for theme $from -> $to", if (from == to) 0 else 1, actions.count { it === SettingsViewAction.Rebirth })
            }
        }
    }

    @Test
    fun iconShapeChoiceIsStoredIncludingNone() {
        start()
        val shape = "M0,0 L10,0 L10,10 L0,10 Z"

        send(SettingsViewEvent.UpdateIconsShape(shape))
        assertEquals(shape, preferences.iconShape)

        send(SettingsViewEvent.UpdateIconsShape(""))
        assertEquals("", preferences.iconShape)
    }

    @Test
    fun crashReportChoiceIsStored() {
        start()

        send(SettingsViewEvent.UpdateCrashReports(false))
        assertFalse(preferences.collectCrashReports)

        send(SettingsViewEvent.UpdateCrashReports(true))
        assertTrue(preferences.collectCrashReports)
    }

    @Test
    fun listPreferenceChangesAreAppliedAndShownInTheItems() {
        start()

        send(SettingsViewEvent.UpdateListPreference(true) { preferences.showOnDevice = it })
        send(SettingsViewEvent.UpdateListPreference(false) { preferences.showRecent = it })

        assertTrue(preferences.showOnDevice)
        assertFalse(preferences.showRecent)
        awaitUntil("the list to show both values") { checked("show-on-device") && !checked("show-recent") }
    }

    @Test
    fun grantingNotificationPermissionUnlocksTheUpdateFrequency() {
        notifications.areNotificationsEnabled = false
        start()

        send(SettingsViewEvent.NotificationPermissionRequest)
        assertEquals(listOf<SettingsViewAction>(SettingsViewAction.RequestNotificationPermission), actions.toList())

        notifications.areNotificationsEnabled = true
        send(SettingsViewEvent.NotificationPermissionResult(granted = true))

        assertTrue(viewModel.viewStates.value.areNotificationsEnabled)
        assertTrue(item("update_frequency").enabled)
        assertEquals(preferences.updatesFrequency.toString(), value("update_frequency"))
    }

    @Test
    fun resumingTheScreenPicksUpNotificationAccessChangedInSystemSettings() {
        notifications.areNotificationsEnabled = false
        start()

        notifications.areNotificationsEnabled = true
        send(SettingsViewEvent.CheckNotificationPermission)
        assertTrue(viewModel.viewStates.value.areNotificationsEnabled)
        assertTrue(item("update_frequency").enabled)

        notifications.areNotificationsEnabled = false
        send(SettingsViewEvent.CheckNotificationPermission)
        assertFalse(viewModel.viewStates.value.areNotificationsEnabled)
        assertFalse(item("update_frequency").enabled)
    }

    @Test
    fun appSettingsShortcutOpensTheAppDetailsScreenOfThisApp() {
        start()

        send(SettingsViewEvent.ShowAppSettings)

        val intent = (actions.single() as SettingsViewAction.StartActivity).intent
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.fromParts("package", app.packageName, null), intent.data)
    }

    @Test
    fun navigationItemsEmitTheirDestinations() {
        start()

        send(SettingsViewEvent.NavigateBack)
        send(SettingsViewEvent.OpenUserLog)
        send(SettingsViewEvent.OpenRefreshHistory)
        send(SettingsViewEvent.OssLicenses)

        awaitUntil("all four actions") { actions.size == 4 }
        assertEquals(SettingsViewAction.NavigateBack, actions[0])
        assertEquals(SettingsViewAction.NavigateTo(SceneNavKey.UserLog), actions[1])
        assertEquals(SettingsViewAction.NavigateTo(SceneNavKey.RefreshHistory), actions[2])
        assertEquals(OssLicensesMenuActivity::class.java.name, (actions[3] as SettingsViewAction.StartActivity).intent.component?.className)
    }

    @Test
    fun testNotificationPostsASampleUpdateNotification() {
        start()

        send(SettingsViewEvent.TestNotification)

        val (id, notification) = notifications.posted.single()
        assertEquals(SyncNotification.SYNC_NOTIFICATION_ID, id)
        assertEquals(SyncNotification.UPDATES_CHANNEL_ID, notification.channelId)
        assertEquals(app.getString(R.string.notification_one_updated, "Test"), notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
    }

    @Test
    fun databaseCleanupDropsRefreshHistoryOlderThanThirtyDaysAndRecordsTheRun() {
        val now = System.currentTimeMillis()
        runBlocking {
            SchedulesTable.Queries.insert(schedule(now - 31 * DateUtils.DAY_IN_MILLIS), database)
            SchedulesTable.Queries.insert(schedule(now - DateUtils.DAY_IN_MILLIS), database)
        }
        start()

        send(SettingsViewEvent.DbCleanup)
        awaitUntil("the cleanup to finish") { preferences.lastCleanupTime != -1L }

        assertEquals(listOf(now - DateUtils.DAY_IN_MILLIS), runBlocking { database.schedules().load().first() }.map { it.start })
        assertTrue(preferences.lastCleanupTime >= now)
    }

    @Test
    fun turningDriveSyncOffStoresTheChoiceWithoutProgress() {
        preferences.isDriveSyncEnabled = true
        start()

        send(SettingsViewEvent.GDriveSyncToggle(false))

        assertFalse(preferences.isDriveSyncEnabled)
        assertFalse(viewModel.viewStates.value.isProgressVisible)
    }

    @Test
    fun syncNowWithoutASignedInAccountExplainsWhyNothingHappened() {
        start()

        send(SettingsViewEvent.GDriveSyncNow)

        awaitUntil("the toast") { actions.isNotEmpty() }
        assertEquals(R.string.no_gdrive_account, (actions.single() as SettingsViewAction.ShowToast).resId)
        assertFalse(viewModel.viewStates.value.isProgressVisible)
    }

    @Test
    fun diagnosticsToggleStartsAndStopsRecordingForAnAllowedAccount() {
        preferences.account = AuthAccount("diagnostic-account", "account-type", "", "", "")
        start()

        send(SettingsViewEvent.DiagnosticToggle(true))
        awaitUntil("recording to start") { preferences.isDiagnosticsEnabled && checked("diagnostics-enabled") }

        send(SettingsViewEvent.DiagnosticToggle(false))
        awaitUntil("recording to stop") { !preferences.isDiagnosticsEnabled && !checked("diagnostics-enabled") }
    }

    @Test
    fun diagnosticsToggleReportsAStorageFailure() {
        preferences.account = AuthAccount("diagnostic-account", "account-type", "", "", "")
        File(app.noBackupFilesDir, "diagnostics").writeText("not a directory")
        start()

        send(SettingsViewEvent.DiagnosticToggle(true))

        awaitUntil("the error toast") { actions.isNotEmpty() }
        assertEquals(R.string.diagnostics_error, (actions.single() as SettingsViewAction.ShowToast).resId)
        awaitUntil("the storage error in the list") { item("diagnostics-enabled").summary.endsWith(app.getString(R.string.diagnostics_error)) }
    }

    @Test
    fun exportWritesTheWatchListAndReportsProgressThenSuccess() {
        runBlocking { database.insertWatchedApp("exported") }
        val output = ByteArrayOutputStream()
        Shadows.shadowOf(app.contentResolver).registerOutputStream(backupUri, output)
        start()

        send(SettingsViewEvent.Export(backupUri))

        awaitUntil("the export to finish") { exportResults() == listOf(-1, DbBackupManager.RESULT_OK) }
        assertFalse(viewModel.viewStates.value.isProgressVisible)
        assertTrue(output.toString().contains("exported.package"))
    }

    @Test
    fun importRestoresTheWatchListFromABackupAndReportsProgressThenSuccess() {
        val backup = runBlocking {
            database.insertWatchedApp("restored")
            StringWriter().also { DbJsonWriter().write(it, database) }.toString()
        }
        runBlocking { database.apps().delete() }
        Shadows.shadowOf(app.contentResolver).registerInputStream(backupUri, ByteArrayInputStream(backup.toByteArray()))
        start()

        send(SettingsViewEvent.Import(backupUri))

        awaitUntil("the import to finish") { importResults() == listOf(-1, DbBackupManager.RESULT_OK) }
        assertFalse(viewModel.viewStates.value.isProgressVisible)
        assertNotNull(runBlocking { database.apps().loadApp("restored") })
    }

    @Test
    fun importingAnInvalidBackupReportsTheErrorAndKeepsTheWatchList() {
        runBlocking { database.insertWatchedApp("kept") }
        Shadows.shadowOf(app.contentResolver).registerInputStream(backupUri, ByteArrayInputStream("this is not a backup".toByteArray()))
        start()

        send(SettingsViewEvent.Import(backupUri))

        awaitUntil("the import to fail") { importResults() == listOf(-1, DbBackupManager.ERROR_DESERIALIZE) }
        assertFalse(viewModel.viewStates.value.isProgressVisible)
        assertNotNull(runBlocking { database.apps().loadApp("kept") })
    }

    @Test
    fun cancelledDriveSignInTurnsDriveSyncOffAndReportsTheResultCode() {
        preferences.isDriveSyncEnabled = true
        start()

        send(driveSignInResult(Activity.RESULT_CANCELED, null))

        awaitUntil("the login error") { actions.isNotEmpty() }
        assertFalse(preferences.isDriveSyncEnabled)
        assertEquals("Drive login error ${Activity.RESULT_CANCELED}", (actions.single() as SettingsViewAction.ShowToast).text)
        assertFalse(viewModel.viewStates.value.isProgressVisible)
    }

    @Test
    fun driveSignInResultWithoutAnAccountReportsTheGoogleApiErrorAndKeepsDriveSyncOff() {
        preferences.isDriveSyncEnabled = true
        start()

        send(driveSignInResult(Activity.RESULT_OK, Intent().putExtra("unrelated", "extra")))

        awaitUntil("the login error") { actions.isNotEmpty() }
        assertFalse(preferences.isDriveSyncEnabled)
        assertEquals("Drive login error ${CommonStatusCodes.INTERNAL_ERROR}", (actions.single() as SettingsViewAction.ShowToast).text)
        assertFalse(viewModel.viewStates.value.isProgressVisible)
    }

    @Test
    fun signedInDriveAccountKeepsDriveSyncOnAndUploadsWatchListChangesWithTheUpdateConstraints() {
        preferences.isWifiOnly = true
        preferences.isRequiresCharging = true
        start()

        send(driveSignInResult(Activity.RESULT_OK, Intent().putExtra("googleSignInAccount", GoogleSignInAccount.createDefault())))

        awaitUntil("the connected message") { actions.isNotEmpty() }
        assertTrue(preferences.isDriveSyncEnabled)
        assertEquals(R.string.gdrive_connected, (actions.single() as SettingsViewAction.ShowToast).resId)
        assertFalse(viewModel.viewStates.value.isProgressVisible)
        assertTrue(scheduledDriveUploads().isEmpty())

        runBlocking { database.insertWatchedApp("changed") }

        awaitUntil("the upload to be scheduled") { scheduledDriveUploads().isNotEmpty() }
        val constraints = scheduledDriveUploads().single().constraints
        assertEquals(NetworkType.UNMETERED, constraints.requiredNetworkType)
        assertTrue(constraints.requiresCharging())
    }

    @Test
    fun syncNowWithoutASignedInDriveAccountExplainsItAndShowsNoProgress() {
        preferences.isDriveSyncEnabled = true
        start()

        send(SettingsViewEvent.GDriveSyncNow)

        assertEquals(R.string.no_gdrive_account, (actions.single() as SettingsViewAction.ShowToast).resId)
        assertFalse(viewModel.viewStates.value.isProgressVisible)
        assertTrue(preferences.isDriveSyncEnabled)
    }
}