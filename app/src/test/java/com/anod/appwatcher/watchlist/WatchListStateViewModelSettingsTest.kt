package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.PackageStateCache
import info.anodsplace.notification.NotificationManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** How the watch list screens pick up the list related settings, both when they open and while they are open. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class WatchListStateViewModelSettingsTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val preferences = Preferences(context, NotificationManager.NoOp(), appScope, DiagnosticAllowlist(emptySet()))
    private val viewModelStore = ViewModelStore()
    private val allFilters = listOf(Filters.ALL, Filters.INSTALLED, Filters.UNINSTALLED, Filters.UPDATABLE)
    private var opened = 0
    private lateinit var database: AppsDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppsDatabase::class.java).allowMainThreadQueries().build()
        startKoin {
            modules(module {
                single<Application> { context }
                single<Context> { context }
                single { preferences }
                single { database }
                single { PackageChangedReceiver() }
                single { PackageStateCache(context.packageManager) }
                factory { RecentlyInstalledAppsLoader(context.packageManager, database) }
            })
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        stopKoin()
        database.close()
        appScope.cancel()
    }

    /** Opens the list the way the main screen does: the settings it hands over are the ones read from the preferences. */
    private fun openList(
        state: SavedStateHandle = SavedStateHandle(),
        tagFilter: WatchListTagFilter = WatchListTagFilter.None,
    ): WatchListStateViewModel {
        val shown = watchListPreferences(preferences)
        val created = WatchListStateViewModel(
            state = state,
            tag = Tag.empty,
            defaultFilterId = shown.defaultFilterId,
            tagFilter = tagFilter,
            showOnDeviceApps = shown.showOnDeviceApps,
            showRecentlyInstalledApps = shown.showRecentlyInstalledApps,
        )
        viewModelStore.put("list-${opened++}", created)
        idle()
        return created
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun awaitUntil(description: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (!condition()) {
            if (System.nanoTime() > deadline) fail("Timed out waiting for $description")
            idle()
            Thread.sleep(10)
        }
    }

    private fun WatchListStateViewModel.settingsChanged() = handleEvent(WatchListEvent.UpdatePreferences(watchListPreferences(preferences)))

    private fun WatchListStateViewModel.config(filterId: Int) = listPagerFactory(filterId, Tag.empty).pagingSourceConfig

    private fun WatchListStateViewModel.recentlyInstalled() = viewStates.value.recentlyInstalledApps.orEmpty()
        .map { it.packageName }
        .filter { it.startsWith("test.") }

    @Suppress("DEPRECATION")
    private fun install(packageName: String, updateTime: Long) {
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                this.packageName = packageName
                versionCode = 1
                versionName = "1"
                lastUpdateTime = updateTime
                applicationInfo = ApplicationInfo().apply {
                    this.packageName = packageName
                    flags = 0
                    enabled = true
                }
            }
        )
    }

    @Test
    fun pullToRefreshIsOnWhenTheSettingIsOn() {
        preferences.enablePullToRefresh = true

        assertTrue(openList().viewStates.value.enablePullToRefresh)
    }

    @Test
    fun pullToRefreshIsOffWhenTheSettingIsOff() {
        preferences.enablePullToRefresh = false

        assertFalse(openList().viewStates.value.enablePullToRefresh)
    }

    @Test
    fun theListOpensOnTheDefaultFilterOfTheSettings() {
        preferences.defaultMainFilterId = Filters.UPDATABLE

        assertEquals(Filters.UPDATABLE, openList().viewStates.value.filterId)
    }

    @Test
    fun theListOpensOnTheTabItWasLeftOnInsteadOfTheDefaultFilter() {
        preferences.defaultMainFilterId = Filters.UPDATABLE

        assertEquals(Filters.INSTALLED, openList(SavedStateHandle(mapOf("tab_id" to Filters.INSTALLED))).viewStates.value.filterId)
        assertEquals(Filters.UNINSTALLED, openList(SavedStateHandle(mapOf("tab_id" to "2"))).viewStates.value.filterId)
    }

    @Test
    fun theListOpensOnTheDefaultFilterWhenLaunchedFromANotificationOrTheSearchShortcut() {
        preferences.defaultMainFilterId = Filters.UPDATABLE

        val fromNotification = openList(SavedStateHandle(mapOf("tab_id" to Filters.INSTALLED, AppWatcherActivity.EXTRA_FROM_NOTIFICATION to true)))
        val toSearch = openList(SavedStateHandle(mapOf("tab_id" to Filters.INSTALLED, "expand_search" to true)))

        assertEquals(Filters.UPDATABLE, fromNotification.viewStates.value.filterId)
        assertFalse(fromNotification.viewStates.value.showSearch)
        assertEquals(Filters.UPDATABLE, toSearch.viewStates.value.filterId)
        assertTrue(toSearch.viewStates.value.showSearch)
    }

    @Test
    fun theListStartsWithTheSavedSortOrderAndRemembersANewOne() {
        preferences.sortIndex = 2
        val list = openList()
        assertEquals(2, list.viewStates.value.sortId)

        list.handleEvent(WatchListEvent.ChangeSort(1))

        assertEquals(1, list.viewStates.value.sortId)
        assertEquals(1, preferences.sortIndex)
    }

    @Test
    fun appsOnTheDeviceAndRecentlyInstalledAppsAreListedOnlyInTheAllFilter() {
        preferences.showOnDevice = true
        preferences.showRecent = true
        val list = openList()

        assertTrue(list.config(Filters.ALL).showOnDevice)
        assertTrue(list.config(Filters.ALL).showRecentlyInstalled)
        for (filter in allFilters - Filters.ALL) {
            assertFalse("on device in filter $filter", list.config(filter).showOnDevice)
            assertFalse("recently installed in filter $filter", list.config(filter).showRecentlyInstalled)
        }
    }

    @Test
    fun appsOnTheDeviceAndRecentlyInstalledAppsAreNotListedWhenTheirSettingsAreOff() {
        preferences.showOnDevice = false
        preferences.showRecent = false
        val list = openList()

        for (filter in allFilters) {
            assertFalse("on device in filter $filter", list.config(filter).showOnDevice)
            assertFalse("recently installed in filter $filter", list.config(filter).showRecentlyInstalled)
        }
    }

    @Test
    fun recentlyDiscoveredAppsFollowTheSettingInEveryFilter() {
        for (shown in listOf(true, false)) {
            preferences.showRecentlyDiscovered = shown
            val list = openList()
            for (filter in allFilters) {
                assertEquals("filter $filter, setting $shown", shown, list.config(filter).showRecentlyDiscovered)
            }
        }
    }

    @Test
    fun theTagFilterDecidesWhichTagTheListShows() {
        assertEquals(null, openList(tagFilter = WatchListTagFilter.None).config(Filters.ALL).tagId)
        assertEquals(Tag.empty.id, openList(tagFilter = WatchListTagFilter.Untagged).config(Filters.ALL).tagId)
        assertEquals(7, openList(tagFilter = WatchListTagFilter.Tag(7)).config(Filters.ALL).tagId)
    }

    @Test
    fun turningPullToRefreshOffInTheSettingsReachesTheOpenList() {
        val list = openList()
        assertTrue(list.viewStates.value.enablePullToRefresh)

        preferences.enablePullToRefresh = false
        list.settingsChanged()

        assertFalse(list.viewStates.value.enablePullToRefresh)
    }

    @Test
    fun changingWhatTheListShowsInTheSettingsRebuildsItsPages() {
        val list = openList()
        val before = list.listPagerFactory(Filters.ALL, Tag.empty)
        assertFalse(before.pagingSourceConfig.showOnDevice)
        assertTrue(before.pagingSourceConfig.showRecentlyDiscovered)

        preferences.showOnDevice = true
        preferences.showRecentlyDiscovered = false
        list.settingsChanged()

        val after = list.listPagerFactory(Filters.ALL, Tag.empty)
        assertNotSame(before, after)
        assertTrue(after.pagingSourceConfig.showOnDevice)
        assertFalse(after.pagingSourceConfig.showRecentlyDiscovered)
        assertEquals(1, list.viewStates.value.listConfigChange)
    }

    @Test
    fun theListIsLeftAloneWhenTheSettingsDidNotChange() {
        val list = openList()
        val before = list.listPagerFactory(Filters.ALL, Tag.empty)

        list.settingsChanged()

        assertSame(before, list.listPagerFactory(Filters.ALL, Tag.empty))
        assertEquals(0, list.viewStates.value.listConfigChange)
    }

    @Test
    fun theNewDefaultFilterTakesOverWhenItChangesInTheSettings() {
        val list = openList()
        assertEquals(Filters.ALL, list.viewStates.value.filterId)

        preferences.defaultMainFilterId = Filters.UNINSTALLED
        list.settingsChanged()

        assertEquals(Filters.UNINSTALLED, list.viewStates.value.filterId)
    }

    @Test
    fun theRecentlyInstalledSectionIsLoadedWhenTheSettingIsOn() {
        install("test.older", updateTime = 1_000)
        install("test.newer", updateTime = 2_000)
        preferences.showRecent = true

        val list = openList()

        awaitUntil("the recently installed apps") { list.viewStates.value.recentlyInstalledApps != null }
        assertEquals(listOf("test.newer", "test.older"), list.recentlyInstalled())
    }

    @Test
    fun theRecentlyInstalledSectionFollowsTheSettingWhileTheListIsOpen() {
        install("test.app", updateTime = 1_000)
        preferences.showRecent = false
        val list = openList()
        assertNull(list.viewStates.value.recentlyInstalledApps)

        preferences.showRecent = true
        list.settingsChanged()
        awaitUntil("the recently installed apps to appear") { list.recentlyInstalled() == listOf("test.app") }

        preferences.showRecent = false
        list.settingsChanged()
        awaitUntil("the recently installed apps to disappear") { list.viewStates.value.recentlyInstalledApps == null }
    }
}