package com.anod.appwatcher

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Composes and measures every Material3 `TextField` surface of the app on a real device.
 *
 * `play-services-oss-licenses` hard-requires its own `compose-material3` build, and Gradle's
 * highest-version-wins once let that override the Compose BOM. The resulting ABI skew against
 * `compose-foundation` crashed every `TextField` while it was measured, so these tests reveal the
 * field and type into it rather than only asserting that navigation reached the screen.
 *
 * The walk is deliberately a single test: the six search bars are all the same `SearchTopBar`
 * composable with different arguments, and an extra `@Test` costs a full activity launch. The
 * licenses screen is separate because it is a separate activity owned by Play services.
 */
@RunWith(AndroidJUnit4::class)
class SearchSurfacesUiTest {
    private val compose = createAndroidComposeRule<AppWatcherActivity>()
    private val support = AppWatcherUiTestSupport(compose)

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(StableStartupPreferencesRule())
        .around(compose)

    @Before
    fun waitForApp() {
        support.awaitAppReady()
    }

    @Test
    fun searchSurfaces_composeMeasureAndAcceptInput(): Unit = with(support) {
        typeIntoTextField(query = "gmail", surface = "watch list")
        returnToWatchList()

        // The market search opens with its field already visible, the rest reveal it on demand.
        listOf(
            R.string.navdrawer_item_add to "market search",
            R.string.installed to "installed",
            R.string.navdrawer_item_wishlist to "wishlist",
            R.string.navdrawer_item_purchases to "purchases"
        ).forEach { (titleRes, surface) ->
            openDrawerItem(text(titleRes), fromEnd = false)
            typeIntoTextField(query = "gmail", surface = surface)
            returnToWatchList()
        }

        // The tag dialog is the app's only other TextField call site, and the only labelled one.
        openDrawerItem(text(R.string.menu_add), fromEnd = true)
        typeIntoTextField(query = "games", surface = "edit tag dialog")
    }

    @Test
    fun ossLicenses_screenOpens(): Unit = with(support) {
        openDrawerItem(text(R.string.navdrawer_item_settings), fromEnd = false)
        waitForText(text(R.string.navdrawer_item_settings))

        val licenses = text(R.string.pref_title_opensource)
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(licenses))

        assertActivityOpens(OSS_LICENSES_ACTIVITY) {
            compose.onNodeWithText(licenses).performClick()
        }
    }

    private companion object {
        const val OSS_LICENSES_ACTIVITY = "com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity"
    }
}