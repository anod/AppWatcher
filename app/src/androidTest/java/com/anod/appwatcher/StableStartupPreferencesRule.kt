package com.anod.appwatcher

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/**
 * Puts the app into a deterministic startup state for UI tests: no background update checks, no
 * crash reporting, and no device registration left over from an earlier run.
 *
 * Apply it outside the Compose rule with a [org.junit.rules.RuleChain] so the preferences are in
 * place before the activity launches. The previous values are restored afterwards, so running the
 * tests does not leave a developer's device reconfigured.
 */
class StableStartupPreferencesRule : ExternalResource() {
    private lateinit var preferences: SharedPreferences
    private lateinit var devicePreferences: SharedPreferences
    private var originalPreferences: Map<String, Any?> = emptyMap()
    private var originalDevicePreferences: Map<String, Any?> = emptyMap()

    override fun before() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        devicePreferences = context.getSharedPreferences(DEVICE_PREFS_NAME, Context.MODE_PRIVATE)
        originalPreferences = HashMap<String, Any?>(preferences.all)
        originalDevicePreferences = HashMap<String, Any?>(devicePreferences.all)
        devicePreferences.edit().clear().commit()
        preferences.edit()
            .putInt("update_frequency", 0)
            .putBoolean("crash-reports", false)
            .commit()
    }

    override fun after() {
        restore(preferences, originalPreferences)
        restore(devicePreferences, originalDevicePreferences)
    }

    private fun restore(preferences: SharedPreferences, values: Map<String, Any?>) {
        val editor = preferences.edit().clear()
        values.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.commit()
    }

    private companion object {
        const val PREFS_NAME = "WatcherPrefs"
        const val DEVICE_PREFS_NAME = "DeviceRegistration"
    }
}