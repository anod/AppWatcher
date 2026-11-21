package com.anod.appwatcher

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode

/** Finds the rows of the settings screen on a device by the text a user reads. */
class SettingsScreenRobot(private val compose: ComposeTestRule, private val support: AppWatcherUiTestSupport) {
    /** Opens the screen through the navigation drawer and waits for its first row. */
    fun open() {
        support.openDrawerItem(support.text(R.string.navdrawer_item_settings), fromEnd = false)
        support.waitForText(support.text(R.string.pref_title_updates_frequency))
    }

    fun scrollTo(title: String, ignoreCase: Boolean = false) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(title, ignoreCase = ignoreCase))
    }

    fun row(title: String): SemanticsNodeInteraction {
        scrollTo(title)
        return compose.onNode(hasText(title))
    }

    fun switchOf(title: String): SemanticsNodeInteraction {
        scrollTo(title)
        return compose.onNode(isToggleable() and hasAnyAncestor(hasText(title)))
    }

    fun assertSwitch(title: String, on: Boolean) {
        awaitAssertion("\"$title\" to be switched ${if (on) "on" else "off"}") {
            val switch = switchOf(title)
            if (on) switch.assertIsOn() else switch.assertIsOff()
        }
    }

    fun assertRowEnabled(title: String, enabled: Boolean) {
        awaitAssertion("\"$title\" to be ${if (enabled) "enabled" else "disabled"}") {
            val row = row(title)
            if (enabled) row.assertIsEnabled() else row.assertIsNotEnabled()
        }
    }

    fun chip(label: String): SemanticsNodeInteraction = compose.onNode(
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and hasText(label)
    )

    /** Picks [label] in the row named [title]. */
    fun select(title: String, label: String) {
        scrollTo(title)
        chip(label).performClick()
    }

    /** Scrolls to the pick [title] and checks that only the chip at [selectedIndex] is selected. */
    fun assertSelectedChip(title: String, labels: List<String>, selectedIndex: Int) {
        awaitAssertion("only \"${labels[selectedIndex]}\" to be selected in \"$title\"") {
            scrollTo(title)
            labels.forEachIndexed { index, label ->
                if (index == selectedIndex) chip(label).assertIsSelected() else chip(label).assertIsNotSelected()
            }
        }
    }

    /** Waits for [condition], naming what was expected when it never holds. */
    fun await(description: String, timeoutMillis: Long = AWAIT_TIMEOUT_MILLIS, condition: () -> Boolean) {
        try {
            compose.waitUntil(timeoutMillis, condition)
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("Timed out waiting for $description", e)
        }
    }

    /** Retries [assertion] until it passes, because a click can reach the screen a frame later. */
    private fun awaitAssertion(description: String, assertion: () -> Unit) {
        var failure: AssertionError? = null
        try {
            compose.waitUntil(AWAIT_TIMEOUT_MILLIS) {
                try {
                    assertion()
                    true
                } catch (e: AssertionError) {
                    failure = e
                    false
                }
            }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("Timed out waiting for $description", failure ?: e)
        }
    }

    private companion object {
        const val AWAIT_TIMEOUT_MILLIS = 10_000L
    }
}