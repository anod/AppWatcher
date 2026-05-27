package com.anod.appwatcher

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.AfterClass
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppWatcherSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<AppWatcherActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun waitForApp() {
        dismissExternalAccountPicker()
        waitForText(text(R.string.app_name))
    }

    @Test
    fun mainFlow_smokeTest() {
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

        openMenuContaining(
            contentDescription = text(R.string.menu),
            expectedText = text(R.string.navdrawer_item_settings),
            preferLast = false
        )
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

    private fun openMenuContaining(contentDescription: String, expectedText: String, preferLast: Boolean) {
        val nodes = compose.onAllNodesWithContentDescription(contentDescription).fetchSemanticsNodes()
        val indices = nodes.indices.toList().let { if (preferLast) it.reversed() else it }
        for (index in indices) {
            compose.onAllNodesWithContentDescription(contentDescription)[index].performClick()
            compose.waitForIdle()
            if (hasVisibleText(expectedText)) {
                return
            }
            device.pressBack()
            compose.waitForIdle()
        }
        fail("No menu with item \"$expectedText\" was found")
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        compose.waitUntil(timeoutMillis) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun hasVisibleText(text: String): Boolean = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun assertTextExists(text: String) {
        compose.onAllNodesWithText(text).fetchSemanticsNodes().also { nodes ->
            if (nodes.isEmpty()) {
                fail("No node with text \"$text\" was found")
            }
        }
    }

    private fun text(resId: Int): String = targetContext.getString(resId)

    private fun dismissExternalAccountPicker() {
        val targetPackage = targetContext.packageName
        repeat(3) {
            if (device.currentPackageName != targetPackage || device.hasObject(By.text(text(R.string.choose_an_account)))) {
                device.pressBack()
                device.wait(Until.hasObject(By.pkg(targetPackage)), 5_000)
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "WatcherPrefs"
        private lateinit var preferences: SharedPreferences
        private var originalPrefs: Map<String, Any?> = emptyMap()

        @JvmStatic
        @BeforeClass
        fun configureStableStartupState() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            originalPrefs = HashMap<String, Any?>(preferences.all)
            preferences.edit()
                .remove("account_name")
                .remove("account_type")
                .remove("gfs_id")
                .remove("gfs_token")
                .remove("device_config")
                .putInt("update_frequency", 0)
                .putBoolean("crash-reports", false)
                .commit()
        }

        @JvmStatic
        @AfterClass
        fun restoreStartupState() {
            val editor = preferences.edit().clear()
            originalPrefs.forEach { (key, value) ->
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is String -> editor.putString(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
            editor.commit()
        }
    }
}