package info.anodpslace.appwatcher.baselineprofile

import android.content.res.Resources
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/**
 * The critical user journeys captured in the baseline profile.
 *
 * The screens are driven through the real UI so that the profile covers composing and measuring
 * them, not just application startup. Labels are read from the installed app's own resources rather
 * than hardcoded in English, so the journey survives running on a device in another locale.
 */
internal class AppWatcherJourney(private val scope: MacrobenchmarkScope, private val packageName: String) {
    private val device get() = scope.device
    private val resources: Resources = InstrumentationRegistry.getInstrumentation()
        .context
        .packageManager
        .getResourcesForApplication(packageName)

    fun awaitWatchList() {
        dismissExternalWindow()
        check(device.wait(Until.hasObject(By.desc(string("menu"))), TIMEOUT_MILLIS) && isOnWatchList()) {
            "Timed out waiting for the watch list, on ${describeScreen()}"
        }
    }

    /**
     * Opens the search field and types into it.
     *
     * Deliberately not skippable: a silent skip would quietly stop covering the search field, which
     * is both a startup-visible surface and the one that regressed on a material3 ABI skew.
     */
    fun searchWatchList() {
        val searchIcon = device.findObject(By.desc(string("menu_filter")))
        checkNotNull(searchIcon) { "The search icon did not appear, on ${describeScreen()}" }
        clicked(searchIcon)
        val field = device.wait(Until.findObject(By.clazz(EDIT_TEXT)), TIMEOUT_MILLIS)
        checkNotNull(field) { "The search field did not appear, on ${describeScreen()}" }
        tolerateStaleNodes(Unit) { field.text = "a" }
        device.waitForIdle()
        returnToWatchList()
    }

    /** Scrolling only happens when apps are watched, so an empty list is tolerated here. */
    fun scrollWatchList() {
        val list = device.findObject(By.scrollable(true)) ?: return
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(Direction.DOWN)
        device.waitForIdle()
        list.fling(Direction.UP)
        device.waitForIdle()
    }

    fun visitDrawerScreen(titleResName: String) {
        openDrawer()
        val label = string(titleResName)
        val item = device.findObject(By.text(label))
        checkNotNull(item) { "The drawer item $label did not appear, on ${describeScreen()}" }
        clicked(item)
        device.waitForIdle()
        dismissExternalWindow()
        returnToWatchList()
    }

    private fun openDrawer() {
        repeat(MAX_ATTEMPTS) {
            dismissExternalWindow()
            val button = drawerButton()
            if (button != null && clicked(button)) {
                if (device.wait(Until.hasObject(By.text(string("navdrawer_item_settings"))), TIMEOUT_MILLIS)) {
                    return
                }
            }
            device.waitForIdle()
        }
        error("The navigation drawer did not open, on ${describeScreen()}")
    }

    /** The watch list is the only screen with a drawer button and no open search field. */
    private fun isOnWatchList(): Boolean = drawerButton() != null && !device.hasObject(By.clazz(EDIT_TEXT))

    /**
     * The top bar carries two actions described as "Menu", the drawer button and the overflow menu.
     * They are told apart by position rather than by the order they are reported in.
     */
    private fun drawerButton(): UiObject2? = tolerateStaleNodes(null) {
        device.findObjects(By.desc(string("menu")))
            .filter { it.visibleBounds.left < device.displayWidth / 2 }
            .minByOrNull { it.visibleBounds.left }
    }

    private fun clicked(target: UiObject2): Boolean = tolerateStaleNodes(false) {
        target.click()
        true
    }

    /**
     * Nodes go stale whenever the screen changes between finding one and reading it, which happens
     * constantly while navigating. Every caller here polls, so losing a node is not a failure.
     */
    private fun <T> tolerateStaleNodes(fallback: T, block: () -> T): T = try {
        block()
    } catch (e: StaleObjectException) {
        fallback
    }

    /**
     * Returns to the watch list using the app's own back arrow.
     *
     * The system back key is deliberately never used here: it is consumed first by the soft keyboard
     * and then by an open search field, so pressing it blindly walks out of the app instead. When a
     * screen offers no way back the app is restarted, which is slower but always lands somewhere the
     * next journey step can start from.
     */
    private fun returnToWatchList() {
        repeat(MAX_BACK_PRESSES) {
            dismissExternalWindow()
            if (isOnWatchList()) {
                return
            }
            val backArrow = device.findObject(By.desc(string("back"))) ?: return restart()
            clicked(backArrow)
            device.waitForIdle()
        }
        restart()
    }

    private fun restart() {
        scope.killProcess()
        scope.startActivityAndWait()
        awaitWatchList()
    }

    /**
     * Clears anything the app put in front of itself.
     *
     * A freshly installed build asks Play Services to pick an account, and the account screens are
     * owned by another package, so the app's own UI is not reachable until they are dismissed.
     */
    private fun dismissExternalWindow() {
        repeat(MAX_BACK_PRESSES) {
            if (device.hasObject(By.pkg(packageName))) {
                return
            }
            device.pressBack()
            device.wait(Until.hasObject(By.pkg(packageName)), TIMEOUT_MILLIS)
        }
    }

    private fun string(name: String): String {
        val id = resources.getIdentifier(name, "string", RESOURCE_PACKAGE)
        check(id != 0) { "String resource $name was not found in $RESOURCE_PACKAGE" }
        return resources.getString(id)
    }

    /** Names what is on screen, so a journey that stalls says where it stalled. */
    private fun describeScreen(): String = tolerateStaleNodes("${device.currentPackageName} showing nothing") {
        val labels = device.findObjects(By.pkg(packageName))
            .mapNotNull { it.text ?: it.contentDescription }
            .distinct()
        "${device.currentPackageName} showing $labels"
    }

    private companion object {
        const val EDIT_TEXT = "android.widget.EditText"

        // The app's resource package is its namespace, which the debug build's applicationId suffix
        // does not change.
        const val RESOURCE_PACKAGE = "com.anod.appwatcher"
        const val TIMEOUT_MILLIS = 10_000L
        const val MAX_BACK_PRESSES = 6
        const val MAX_ATTEMPTS = 3
    }
}