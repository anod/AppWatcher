package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.sync.UpdatedApp
import com.anod.appwatcher.watchlist.WatchListPreferences
import com.anod.appwatcher.watchlist.watchListPreferenceKeys
import com.anod.appwatcher.watchlist.watchListPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Checks that the screens which consume the settings really follow what is stored by Settings. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class SettingsConsumersTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val prefs = Preferences(context, TestNotificationManager(), scope, DiagnosticAllowlist(emptySet()))

    @After
    fun tearDown() {
        scope.cancel()
    }

    private val notInstalled = updatedApp("not-installed", installedVersionCode = 0, versionNumber = 5)
    private val installedWithUpdate = updatedApp("installed-with-update", installedVersionCode = 4, versionNumber = 5)
    private val installedUpToDate = updatedApp("installed-up-to-date", installedVersionCode = 5, versionNumber = 5)
    private val withoutNewDetails = updatedApp("without-new-details", installedVersionCode = 0, versionNumber = 5, noNewDetails = true)
    private val allUpdates = listOf(notInstalled, installedWithUpdate, installedUpToDate, withoutNewDetails)

    private fun updatedApp(
        packageName: String,
        installedVersionCode: Int,
        versionNumber: Int,
        noNewDetails: Boolean = false
    ) = UpdatedApp(
        packageName = packageName,
        versionNumber = versionNumber,
        title = packageName,
        uploadTime = 100,
        uploadDate = "100",
        recentChanges = "",
        installedVersionCode = installedVersionCode,
        installedEnabled = installedVersionCode > 0,
        isNewUpdate = false,
        noNewDetails = noNewDetails
    )

    private fun notified() = SyncNotification.Filter(prefs).apply(allUpdates)

    @Test
    fun everyUpdateIsNotifiedByDefault() {
        assertFalse(SyncNotification.Filter(prefs).hasFilters)
        assertEquals(allUpdates, notified())
    }

    @Test
    fun turningOffInstalledAppsHidesTheUpdatesOfInstalledApps() {
        prefs.isNotifyInstalled = false

        assertTrue(SyncNotification.Filter(prefs).hasFilters)
        assertEquals(listOf(notInstalled, withoutNewDetails), notified())
    }

    @Test
    fun turningOffUpToDateAppsHidesOnlyTheAppsAlreadyUpdatedOnTheDevice() {
        prefs.isNotifyInstalledUpToDate = false

        assertTrue(SyncNotification.Filter(prefs).hasFilters)
        assertEquals(listOf(notInstalled, installedWithUpdate, withoutNewDetails), notified())
    }

    @Test
    fun turningOffNoChangesHidesTheUpdatesWithoutNewDetails() {
        prefs.isNotifyNoChanges = false

        assertTrue(SyncNotification.Filter(prefs).hasFilters)
        assertEquals(listOf(notInstalled, installedWithUpdate, installedUpToDate), notified())
    }

    @Test
    fun turningOffEveryNotificationKeepsOnlyTheUpdatesOfAppsThatAreNotInstalled() {
        prefs.isNotifyInstalled = false
        prefs.isNotifyInstalledUpToDate = false
        prefs.isNotifyNoChanges = false

        assertEquals(listOf(notInstalled), notified())
    }

    @Test
    fun turningTheNotificationsBackOnRestoresEveryUpdate() {
        prefs.isNotifyInstalled = false
        prefs.isNotifyNoChanges = false
        prefs.isNotifyInstalled = true
        prefs.isNotifyNoChanges = true

        assertFalse(SyncNotification.Filter(prefs).hasFilters)
        assertEquals(allUpdates, notified())
    }

    private class ListSetting(
        val key: String,
        val change: (Preferences) -> Unit,
        val shownIn: (WatchListPreferences) -> Any
    )

    private val listSettings = listOf(
        ListSetting(Preferences.DEFAULT_MAIN_FILTER_ID, { it.defaultMainFilterId = Filters.UPDATABLE }) { it.defaultFilterId },
        ListSetting(Preferences.SHOW_ON_DEVICE, { it.showOnDevice = true }) { it.showOnDeviceApps },
        ListSetting(Preferences.SHOW_RECENT, { it.showRecent = false }) { it.showRecentlyInstalledApps },
        ListSetting(Preferences.SHOW_RECENTLY_DISCOVERED, { it.showRecentlyDiscovered = false }) { it.showRecentlyDiscoveredApps },
        ListSetting(Preferences.PULL_TO_REFRESH, { it.enablePullToRefresh = false }) { it.enablePullToRefresh },
        ListSetting(Preferences.ICON_SHAPE, { it.iconShape = "M0,0L10,10" }) { it.iconShape }
    )

    @Test
    fun theListStartsWithTheDefaultsOfTheSettings() {
        assertEquals(WatchListPreferences(iconShape = prefs.iconShape), watchListPreferences(prefs))
    }

    @Test
    fun everyListSettingIsWatchedForChanges() {
        assertEquals(watchListPreferenceKeys, listSettings.map { it.key }.toSet())
    }

    @Test
    fun eachListSettingChangesOnlyItsOwnPartOfTheList() {
        listSettings.forEach { changed ->
            val before = watchListPreferences(prefs)

            changed.change(prefs)

            val after = watchListPreferences(prefs)
            assertNotEquals("${changed.key} must reach the list", changed.shownIn(before), changed.shownIn(after))
            listSettings.filter { it !== changed }.forEach { other ->
                assertEquals("${other.key} after changing ${changed.key}", other.shownIn(before), other.shownIn(after))
            }
        }
    }

    @Test
    fun changingAListSettingAnnouncesAWatchedKey() {
        val keys = mutableListOf<String>()
        scope.launch { prefs.changes.collect { keys += it } }

        listSettings.forEach { setting ->
            keys.clear()

            setting.change(prefs)

            assertEquals(listOf(setting.key), keys)
            assertTrue(setting.key in watchListPreferenceKeys)
        }
    }
}