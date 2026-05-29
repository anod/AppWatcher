// Copyright (c) 2026. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.App as DbApp
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import info.anodsplace.framework.content.InstalledApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SectionItemTest {

    @Test
    fun `section keys are unique for a realistic watchlist generation`() {
        assertUniqueSectionKeys(
            listOf(
                SectionItem.Recent,
                SectionItem.Header(SectionHeader.New),
                appSectionItem(rowId = 1, packageName = "com.example.new"),
                SectionItem.Header(SectionHeader.Watching),
                appSectionItem(rowId = 2, packageName = "com.example.watching"),
                SectionItem.Header(SectionHeader.OnDevice),
                onDeviceSectionItem(packageName = "com.example.installed"),
                SectionItem.Header(SectionHeader.RecentlyInstalled),
                onDeviceSectionItem(packageName = "com.example.recently.installed")
            )
        )
    }

    @Test
    fun `app section key is stable across refreshed app content`() {
        val app = appSectionItem(
            rowId = 1,
            packageName = "com.example.app",
            title = "Original title",
            versionNumber = 1
        )
        val refreshedApp = appSectionItem(
            rowId = 1,
            packageName = "com.example.app",
            title = "Updated title",
            versionNumber = 2
        )

        assertEquals(app.sectionKey, refreshedApp.sectionKey)
    }

    @Test
    fun `app section keys stay unique when temporary row ids match`() {
        assertNotEquals(
            appSectionItem(rowId = -1, packageName = "com.example.first").sectionKey,
            appSectionItem(rowId = -1, packageName = "com.example.second").sectionKey
        )
    }

    @Test
    fun `on device section keys use package name when row id is missing`() {
        val onDevice = onDeviceSectionItem(packageName = "com.example.local")
        val changedOnDevice = onDeviceSectionItem(
            packageName = "com.example.local",
            title = "Updated local title",
            showSelection = true
        )

        assertEquals(onDevice.sectionKey, changedOnDevice.sectionKey)
        assertNotEquals(
            onDevice.sectionKey,
            onDeviceSectionItem(packageName = "com.example.other").sectionKey
        )
    }

    @Test
    fun `header section keys are unique per header type`() {
        assertUniqueSectionKeys(
            listOf(
                SectionItem.Header(SectionHeader.New),
                SectionItem.Header(SectionHeader.RecentlyDiscovered),
                SectionItem.Header(SectionHeader.Watching),
                SectionItem.Header(SectionHeader.RecentlyInstalled),
                SectionItem.Header(SectionHeader.OnDevice),
                SectionItem.Empty
            )
        )
    }

    private fun assertUniqueSectionKeys(items: List<SectionItem>) {
        val duplicateKeys = items
            .groupBy { it.sectionKey }
            .filterValues { it.size > 1 }
            .keys

        assertEquals(emptySet<String>(), duplicateKeys)
    }

    private fun appSectionItem(
        rowId: Int,
        packageName: String,
        title: String = packageName,
        versionNumber: Int = 1
    ): SectionItem.App = SectionItem.App(
        appListItem = appListItem(
            rowId = rowId,
            packageName = packageName,
            title = title,
            versionNumber = versionNumber
        ),
        isLocal = false,
        packageInfo = InstalledApps.Info(versionCode = 0, versionName = "")
    )

    private fun onDeviceSectionItem(
        packageName: String,
        title: String = packageName,
        showSelection: Boolean = false
    ): SectionItem.OnDevice = SectionItem.OnDevice(
        appListItem = appListItem(rowId = -1, packageName = packageName, title = title),
        showSelection = showSelection,
        packageInfo = InstalledApps.Info(versionCode = 1, versionName = "1")
    )

    private fun appListItem(
        rowId: Int,
        packageName: String,
        title: String,
        versionNumber: Int = 1
    ): AppListItem = AppListItem(
        app = DbApp(
            rowId = rowId,
            appId = packageName,
            packageName = packageName,
            versionNumber = versionNumber,
            versionName = versionNumber.toString(),
            title = title,
            creator = "creator",
            iconUrl = "",
            status = 0,
            uploadDate = "",
            price = Price(text = "", cur = "", micros = 0),
            detailsUrl = "",
            uploadTime = 0L,
            appType = "app",
            syncTime = 0L
        ),
        changeDetails = "",
        noNewDetails = false,
        recentFlag = false
    )
}