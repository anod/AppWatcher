package com.anod.appwatcher.watchlist

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import coil3.ImageLoader
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.AppIconLoader
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RecentlyInstalledCardsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun cardsHaveEqualHeightWhenTitlesUseDifferentLineCounts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appIconLoader = AppIconLoader.Simple(context, ImageLoader.Builder(context).build())
        val apps = persistentListOf(
            createApp(rowId = 1, packageName = "eden", title = "Eden"),
            createApp(rowId = 2, packageName = "adaptive", title = "Simply\nAdaptive App")
        )

        compose.setContent {
            AppTheme(updateSystemBars = false) {
                MaterialTheme(
                    typography = MaterialTheme.typography.copy(
                        bodyMedium = MaterialTheme.typography.bodyMedium.copy(lineHeight = 28.sp)
                    )
                ) {
                    WatchListSectionItem(
                        item = SectionItem.Recent,
                        index = 0,
                        onEvent = {},
                        appIconLoader = appIconLoader,
                        recentlyInstalledApps = apps
                    )
                }
            }
        }
        compose.waitForIdle()

        val cardHeights = compose
            .onAllNodes(hasClickAction(), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .map { it.boundsInRoot.height }
        val titleHeights = apps.map {
            compose.onNodeWithText(it.title, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.height
        }

        assertEquals(2, cardHeights.size)
        assertTrue(titleHeights.last() > titleHeights.first())
        assertEquals(cardHeights.first(), cardHeights.last(), 0f)
    }

    private fun createApp(rowId: Int, packageName: String, title: String): App = App.fromLocalPackage(
        rowId = rowId,
        packageName = packageName,
        uploadTime = 0,
        versionCode = 1,
        versionName = "1",
        appTitle = title,
        launchComponent = null
    )
}