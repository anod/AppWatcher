package com.anod.appwatcher.model

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import info.anodsplace.framework.content.InstalledApps
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListFilterTest {

    @Test
    fun updatableFilterExcludesOnlyTheIgnoredVersion() {
        val installedApps = InstalledApps.StaticMap(
            mapOf(PACKAGE_NAME to InstalledApps.Info(versionCode = 100, versionName = "100"))
        )
        val filter = AppListFilter.Updatable(installedApps) { packageName, versionCode ->
            packageName == PACKAGE_NAME && versionCode == 101
        }

        assertTrue(filter.filterRecord(appListItem(versionCode = 101)))
        assertFalse(filter.filterRecord(appListItem(versionCode = 102)))
    }

    private fun appListItem(versionCode: Int) = AppListItem(
        app = App(
            rowId = 1,
            appId = PACKAGE_NAME,
            packageName = PACKAGE_NAME,
            versionNumber = versionCode,
            versionName = versionCode.toString(),
            title = "Example",
            creator = "",
            iconUrl = "",
            status = App.STATUS_UPDATED,
            uploadDate = "",
            price = Price("", "", 0),
            detailsUrl = App.createDetailsUrl(PACKAGE_NAME),
            uploadTime = 0,
            appType = "",
            syncTime = 0,
        ),
        changeDetails = "",
        noNewDetails = false,
        recentFlag = false
    )

    private companion object {
        const val PACKAGE_NAME = "com.example.app"
    }
}