package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WatchListStateViewModelTest {
    @Test
    fun `list preference change refreshes list without changing current filter`() {
        val state = WatchListSharedState(
            tag = Tag.empty,
            sortId = 0,
            filterId = Filters.UPDATABLE,
            enablePullToRefresh = true,
        )

        val updated = watchListStateForPreferencesChange(
            viewState = state,
            currentPreferences = WatchListPreferences(
                defaultFilterId = Filters.ALL,
                showOnDeviceApps = false,
                enablePullToRefresh = true,
            ),
            newPreferences = WatchListPreferences(
                defaultFilterId = Filters.ALL,
                showOnDeviceApps = true,
                enablePullToRefresh = false,
            )
        )

        assertEquals(Filters.UPDATABLE, updated.filterId)
        assertEquals(false, updated.enablePullToRefresh)
        assertEquals(1, updated.listConfigChange)
    }

    @Test
    fun `default filter preference change updates current filter`() {
        val state = WatchListSharedState(
            tag = Tag.empty,
            sortId = 0,
            filterId = Filters.ALL,
        )

        val updated = watchListStateForPreferencesChange(
            viewState = state,
            currentPreferences = WatchListPreferences(defaultFilterId = Filters.ALL),
            newPreferences = WatchListPreferences(defaultFilterId = Filters.UPDATABLE)
        )

        assertEquals(Filters.UPDATABLE, updated.filterId)
        assertEquals(1, updated.listConfigChange)
    }

    @Test
    fun `icon shape preference change refreshes list`() {
        val state = WatchListSharedState(
            tag = Tag.empty,
            sortId = 0,
            filterId = Filters.ALL,
            listConfigChange = 5,
        )

        val updated = watchListStateForPreferencesChange(
            viewState = state,
            currentPreferences = WatchListPreferences(iconShape = "old"),
            newPreferences = WatchListPreferences(iconShape = "new")
        )

        assertEquals(6, updated.listConfigChange)
    }

    @Test
    fun `recently installed preference off clears recent apps`() {
        val state = WatchListSharedState(
            tag = Tag.empty,
            sortId = 0,
            filterId = Filters.ALL,
            recentlyInstalledApps = persistentListOf(
                App(
                    rowId = 1,
                    appId = "package",
                    packageName = "package",
                    versionNumber = 1,
                    versionName = "1",
                    title = "Title",
                    creator = "",
                    iconUrl = "",
                    status = App.STATUS_NORMAL,
                    uploadDate = "",
                    price = Price("", "", 0),
                    detailsUrl = null,
                    uploadTime = 1L,
                    appType = "",
                    syncTime = 1L,
                    recentFlag = false
                )
            ),
        )

        val updated = watchListStateForPreferencesChange(
            viewState = state,
            currentPreferences = WatchListPreferences(showRecentlyInstalledApps = true),
            newPreferences = WatchListPreferences(showRecentlyInstalledApps = false)
        )

        assertNull(updated.recentlyInstalledApps)
    }
}