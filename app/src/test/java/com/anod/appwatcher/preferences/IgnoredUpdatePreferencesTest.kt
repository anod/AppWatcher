package com.anod.appwatcher.preferences

import androidx.test.core.app.ApplicationProvider
import info.anodsplace.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IgnoredUpdatePreferencesTest {

    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        preferences = Preferences(
            context = ApplicationProvider.getApplicationContext(),
            notificationManager = NotificationManager.NoOp(),
            appScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun ignoredUpdateMatchesOnlyStoredVersion() {
        val packageName = "com.example.ignored.exact"

        preferences.setUpdateIgnored(packageName, 101, ignored = true)

        assertTrue(preferences.isUpdateIgnored(packageName, 101))
        assertFalse(preferences.isUpdateIgnored(packageName, 102))

        preferences.setUpdateIgnored(packageName, 101, ignored = false)
    }

    @Test
    fun newerIgnoredVersionReplacesPreviousVersion() {
        val packageName = "com.example.ignored.replaced"

        preferences.setUpdateIgnored(packageName, 101, ignored = true)
        preferences.setUpdateIgnored(packageName, 102, ignored = true)
        preferences.setUpdateIgnored(packageName, 101, ignored = false)

        assertFalse(preferences.isUpdateIgnored(packageName, 101))
        assertTrue(preferences.isUpdateIgnored(packageName, 102))

        preferences.setUpdateIgnored(packageName, 102, ignored = false)
    }
}