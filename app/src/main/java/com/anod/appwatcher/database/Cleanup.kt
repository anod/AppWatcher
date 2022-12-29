package com.anod.appwatcher.database

import android.text.format.DateUtils
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog

class Cleanup(private val pref: Preferences, private val database: AppsDatabase) {
    suspend fun performIfNeeded(now: Long) {
        val cleanupTime = pref.lastCleanupTime
        if (cleanupTime == -1L || now > DateUtils.DAY_IN_MILLIS + cleanupTime) {
            perform(now)
        }
    }

    suspend fun perform(now: Long) {
        AppLog.d("Perform cleanup")
        database.schedules().clean(now - (30 * DateUtils.DAY_IN_MILLIS))
        pref.lastCleanupTime = System.currentTimeMillis()
    }
}