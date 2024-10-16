package com.anod.appwatcher.database

import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.ktx.MILLIS_IN_A_DAY
import info.anodsplace.applog.AppLog
import kotlinx.datetime.Clock

class Cleanup(private val pref: Preferences, private val database: AppsDatabase) {
    suspend fun performIfNeeded(now: Long) {
        val cleanupTime = pref.lastCleanupTime
        if (cleanupTime == -1L || now > MILLIS_IN_A_DAY + cleanupTime) {
            perform(now)
        }
    }

    suspend fun perform(now: Long) {
        AppLog.d("Perform cleanup")
        database.schedules().clean(now - (30 * MILLIS_IN_A_DAY))
        pref.lastCleanupTime = Clock.System.now().toEpochMilliseconds()
    }
}