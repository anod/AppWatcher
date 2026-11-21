package com.anod.appwatcher

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.anod.appwatcher.sync.SyncScheduler
import org.junit.rules.ExternalResource

/** How the periodic update check is scheduled, as WorkManager reports it. */
data class SyncSchedule(val periodSeconds: Long, val wifiOnly: Boolean, val requiresCharging: Boolean)

/** The enqueued update check, or null when none is scheduled (a cancelled one does not count). */
fun currentSyncSchedule(context: Context): SyncSchedule? {
    val work = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWork(SyncScheduler.TAG)
        .get()
        .firstOrNull { !it.state.isFinished } ?: return null
    val period = work.periodicityInfo ?: return null
    return SyncSchedule(
        periodSeconds = period.repeatIntervalMillis / 1000,
        wifiOnly = work.constraints.requiredNetworkType == NetworkType.UNMETERED,
        requiresCharging = work.constraints.requiresCharging()
    )
}

/**
 * Replaces the app's WorkManager with an in-memory one that never runs anything.
 *
 * Changing the update frequency or its constraints on the settings screen reschedules the periodic
 * update check. Against the real WorkManager that would leave a developer's device on another
 * schedule after a test run, and could start a real sync while the device registration is cleared by
 * [StableStartupPreferencesRule]. Apply it outside the Compose rule, so the substitute is in place
 * before the activity launches.
 */
class IsolatedWorkManagerRule : ExternalResource() {
    override fun before() {
        WorkManagerTestInitHelper.initializeTestWorkManager(InstrumentationRegistry.getInstrumentation().targetContext)
    }
}