package com.anod.appwatcher

import android.app.Instrumentation
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail

/**
 * Navigation and assertion helpers shared by the instrumentation tests.
 *
 * The app screens are reached through the real UI rather than through direct navigation calls, so
 * that every top bar is genuinely composed and measured on the device.
 */
class AppWatcherUiTestSupport(private val compose: ComposeTestRule) {
    private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext

    val device: UiDevice = UiDevice.getInstance(instrumentation)

    fun text(resId: Int): String = targetContext.getString(resId)

    /** Clears anything covering the app and waits until the watch list is on screen. */
    fun awaitAppReady() {
        dismissExternalAccountPicker()
        waitForText(text(R.string.app_name))
    }

    fun waitForText(text: String, timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {
        compose.waitUntil(timeoutMillis) { hasVisibleText(text) }
    }

    fun hasVisibleText(text: String): Boolean = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    fun assertTextExists(text: String) {
        if (!hasVisibleText(text)) {
            fail("No node with text \"$text\" was found")
        }
    }

    /**
     * Clicks each node carrying [contentDescription] until one of them reveals [expectedText].
     *
     * The menu has to be recognised by what it opens rather than by its position, because the top
     * bar carries two identically described buttons. Waiting for the text instead of sampling it
     * once matters on a software rendered emulator, where the drawer needs several frames to settle
     * and a single check reports the wrong button as the wrong one.
     */
    fun openMenuContaining(contentDescription: String, expectedText: String, preferLast: Boolean) {
        val nodes = compose.onAllNodesWithContentDescription(contentDescription).fetchSemanticsNodes()
        val indices = nodes.indices.toList().let { if (preferLast) it.reversed() else it }
        for (index in indices) {
            compose.onAllNodesWithContentDescription(contentDescription)[index].performClick()
            if (awaitText(expectedText, MENU_TIMEOUT_MILLIS)) {
                return
            }
            device.pressBack()
            compose.waitForIdle()
        }
        fail("No menu with item \"$expectedText\" was found")
    }

    /** Opens the navigation drawer, which is the only place every top level screen is listed. */
    fun openDrawer() {
        openMenuContaining(
            contentDescription = text(R.string.menu),
            expectedText = text(R.string.navdrawer_item_settings),
            preferLast = false
        )
    }

    /**
     * Opens the drawer and clicks one of its items.
     *
     * [R.string.navdrawer_item_add] and [R.string.menu_add] both read "Add", so [fromEnd] picks
     * between them: the navigation items are rendered above the tags section, and the add-tag item
     * is rendered last.
     */
    fun openDrawerItem(itemText: String, fromEnd: Boolean) {
        openDrawer()
        val items = compose.onAllNodesWithText(itemText)
        val matches = items.fetchSemanticsNodes().size
        if (matches == 0) {
            fail("No drawer item labelled \"$itemText\" was found")
        }
        items[if (fromEnd) matches - 1 else 0].performClick()
        compose.waitForIdle()
        dismissExternalAccountPicker()
    }

    /**
     * Returns to the watch list using the app's own back arrow.
     *
     * The system back key is deliberately not used: with a search field open it dismisses the
     * keyboard and then finishes the activity, which tears down the Compose hierarchy the remaining
     * surfaces still need. The number of presses varies per screen, because some top bars close the
     * search field first and navigate only on the next press.
     */
    fun returnToWatchList() {
        repeat(MAX_BACK_PRESSES) {
            if (searchFieldCount() == 0 && hasVisibleText(text(R.string.app_name))) {
                return
            }
            val backArrow = compose.onAllNodesWithContentDescription(text(R.string.back))
            if (backArrow.fetchSemanticsNodes().isEmpty()) {
                fail("No back arrow was found while returning to the watch list")
            }
            backArrow[0].performClick()
            compose.waitForIdle()
        }
        fail("Could not return to the watch list within $MAX_BACK_PRESSES back presses")
    }

    /**
     * Reveals the text field of the current screen and types into it.
     *
     * This is the regression that matters: an ABI skew between material3 and compose-foundation
     * crashes the field while it is measured, so the field has to be displayed and fed real input,
     * not merely navigated to. Screens that render the field straight away are handled too, because
     * the search icon doubles as the field's leading icon once search is open.
     */
    fun typeIntoTextField(query: String, surface: String) {
        if (searchFieldCount() == 0) {
            val searchIcon = compose.onAllNodesWithContentDescription(text(R.string.menu_filter))
            if (searchIcon.fetchSemanticsNodes().isEmpty()) {
                fail("No search icon was found on $surface")
            }
            searchIcon[0].performClick()
            compose.waitUntil(DEFAULT_TIMEOUT_MILLIS) { searchFieldCount() > 0 }
        }

        val field: SemanticsNodeInteraction = compose.onAllNodes(hasSetTextAction())[0]
        field.assertIsDisplayed()
        field.performTextInput(query)
        compose.waitForIdle()
        field.assert(hasText(query))
    }

    /**
     * Runs [block] and asserts that it brought [activityClass] to the foreground.
     *
     * A `By.pkg` check is not usable here: the licenses screen is declared in the app's own
     * manifest, so it reports the app's package and the wait passes even when nothing opened.
     */
    fun assertActivityOpens(activityClass: String, block: () -> Unit) {
        val monitor = instrumentation.addMonitor(activityClass, null, false)
        try {
            block()
            val activity = monitor.waitForActivityWithTimeout(DEFAULT_TIMEOUT_MILLIS)
            assertNotNull("$activityClass did not open", activity)
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    fun dismissExternalAccountPicker() {
        val targetPackage = targetContext.packageName
        repeat(3) {
            if (device.currentPackageName != targetPackage || device.hasObject(By.text(text(R.string.choose_an_account)))) {
                device.pressBack()
                device.wait(Until.hasObject(By.pkg(targetPackage)), 5_000)
            }
        }
    }

    /** Bounded wait that reports whether [text] appeared rather than failing the test. */
    private fun awaitText(text: String, timeoutMillis: Long): Boolean = try {
        compose.waitUntil(timeoutMillis) { hasVisibleText(text) }
        true
    } catch (_: ComposeTimeoutException) {
        false
    }

    private fun searchFieldCount(): Int = try {
        compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size
    } catch (e: IllegalStateException) {
        // Compose reports "no hierarchies" whenever something else owns the foreground, which is
        // far easier to act on once the offending package is named.
        throw AssertionError("The app's Compose UI is not on screen, foreground package is ${device.currentPackageName}", e)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 10_000L

        // Short, because every candidate button that does not open the wanted menu costs this wait.
        const val MENU_TIMEOUT_MILLIS = 3_000L
        const val MAX_BACK_PRESSES = 6
    }
}