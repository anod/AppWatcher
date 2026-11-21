package com.anod.appwatcher.backup.gdrive

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.preferences.TestNotificationManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
@Config(application = Application::class, sdk = [35])
class UploadServiceContentObserverTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val preferences = Preferences(app, TestNotificationManager(), scope, DiagnosticAllowlist(emptySet()))
    private val observer = UploadServiceContentObserver(app, preferences)

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun uploads(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork("GDriveUpload").get()

    @Test
    fun watchListChangesAreNotUploadedWhileDriveSyncIsOff() {
        preferences.isDriveSyncEnabled = false

        observer.onInvalidated(setOf(AppListTable.TABLE))

        assertTrue(uploads().isEmpty())
    }

    @Test
    fun watchListChangesAreUploadedOnAnyConnectionAfterAShortDelayWhenDriveSyncIsOn() {
        preferences.isDriveSyncEnabled = true

        observer.onInvalidated(setOf(AppListTable.TABLE))

        val upload = uploads().single()
        assertEquals(WorkInfo.State.ENQUEUED, upload.state)
        assertEquals(NetworkType.CONNECTED, upload.constraints.requiredNetworkType)
        assertFalse(upload.constraints.requiresCharging())
        assertEquals(TimeUnit.MINUTES.toMillis(1), upload.initialDelayMillis)
    }

    @Test
    fun uploadsFollowTheWifiOnlyAndChargingSettingsAtTheTimeOfTheChange() {
        preferences.isDriveSyncEnabled = true
        preferences.isWifiOnly = true
        preferences.isRequiresCharging = true

        observer.onInvalidated(setOf(AppListTable.TABLE))

        val upload = uploads().single()
        assertEquals(NetworkType.UNMETERED, upload.constraints.requiredNetworkType)
        assertTrue(upload.constraints.requiresCharging())
    }

    @Test
    fun aNewChangeReplacesThePendingUploadWithTheCurrentSettings() {
        preferences.isDriveSyncEnabled = true
        observer.onInvalidated(setOf(AppListTable.TABLE))
        val first = uploads().single()

        preferences.isWifiOnly = true
        observer.onInvalidated(setOf(AppListTable.TABLE))

        val second = uploads().single()
        assertFalse(first.id == second.id)
        assertEquals(NetworkType.UNMETERED, second.constraints.requiredNetworkType)
    }
}