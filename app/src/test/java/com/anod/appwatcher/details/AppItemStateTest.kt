package com.anod.appwatcher.details

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import info.anodsplace.framework.content.InstalledApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppItemStateTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun disabledInstalledAppDoesNotShowAvailableUpgrade() {
        val state = calcAppItemState(
            app = app(versionNumber = 200, status = App.STATUS_UPDATED),
            recentFlag = true,
            textColor = Color.Black,
            primaryColor = Color.Blue,
            packageInfo = InstalledApps.Info(versionCode = 100, versionName = "1.0"),
            isPackageEnabled = false,
            context = context
        )

        assertTrue(state.installed)
        assertFalse(state.showRecent)
        assertEquals(Color.Black, state.color)
        assertEquals(
            context.getString(R.string.installed_disabled_version, "1.0", 100),
            state.text
        )
    }

    @Test
    fun enabledInstalledAppShowsAvailableUpgrade() {
        val state = calcAppItemState(
            app = app(versionNumber = 200),
            recentFlag = false,
            textColor = Color.Black,
            primaryColor = Color.Blue,
            packageInfo = InstalledApps.Info(versionCode = 100, versionName = "1.0"),
            isPackageEnabled = true,
            context = context
        )

        assertTrue(state.installed)
        assertTrue(state.text.contains("200"))
    }

    private fun app(versionNumber: Int, status: Int = App.STATUS_NORMAL) = App(
        rowId = 1,
        appId = "com.example.app",
        packageName = "com.example.app",
        versionNumber = versionNumber,
        versionName = "2.0",
        title = "Example",
        creator = "Example",
        iconUrl = "",
        status = status,
        uploadDate = "",
        price = Price("", "", 0),
        detailsUrl = null,
        uploadTime = 0,
        appType = "",
        syncTime = 0
    )
}