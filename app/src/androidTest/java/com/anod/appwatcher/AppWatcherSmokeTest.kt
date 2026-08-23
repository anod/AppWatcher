package com.anod.appwatcher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppWatcherSmokeTest {
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
    fun mainFlow_smokeTest(): Unit = with(support) {
        assertTextExists(text(R.string.app_name))

        compose.onNodeWithContentDescription(text(R.string.menu_filter)).performClick()
        assertTextExists(text(R.string.search))
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()

        compose.onNodeWithContentDescription(text(R.string.filter)).performClick()
        assertTextExists(text(R.string.tab_all))
        assertTextExists(text(R.string.tab_installed))
        assertTextExists(text(R.string.tab_not_installed))
        assertTextExists(text(R.string.tab_updatable))
        device.pressBack()

        openMenuContaining(
            contentDescription = text(R.string.menu),
            expectedText = text(R.string.sort),
            preferLast = true
        )
        assertTextExists(text(R.string.menu_refresh))
        assertTextExists(text(R.string.play_store_my_apps))
        device.pressBack()

        openDrawer()
        assertTextExists(text(R.string.navdrawer_item_add))
        assertTextExists(text(R.string.installed))
        assertTextExists(text(R.string.navdrawer_item_wishlist))
        compose.onNodeWithText(text(R.string.navdrawer_item_settings)).performClick()

        assertTextExists(text(R.string.navdrawer_item_settings))
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(text(R.string.refresh_history)))
        compose.onNodeWithText(text(R.string.refresh_history)).performClick()
        assertTextExists(text(R.string.refresh_history))
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()

        compose.onNode(hasScrollAction()).performScrollToNode(hasText(text(R.string.user_log)))
        compose.onNodeWithText(text(R.string.user_log)).performClick()
        assertTextExists(text(R.string.user_log))
        compose.onNodeWithContentDescription(text(R.string.share)).assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()

        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        assertTextExists(text(R.string.app_name))
    }
}