package com.anod.appwatcher.preferences

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticStorageState
import info.anodsplace.compose.ActionablePreferenceItem
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.playservices.GooglePlayServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Shows [SettingsScreen] built from the real [preferenceItems] and records every event it sends. */
@RunWith(RobolectricTestRunner::class)
abstract class SettingsScreenTestBase {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val screenState = mutableStateOf(SettingsViewState(emptyList()))

    protected val context: Context = ApplicationProvider.getApplicationContext()
    protected val notifications = TestNotificationManager()
    protected val prefs = Preferences(context, notifications, scope, DiagnosticAllowlist(emptySet()))
    protected var items = emptyList<PreferenceItem>()
        private set
    val events = mutableListOf<SettingsViewEvent>()

    @Before
    fun showScreen() {
        compose.setContent {
            MaterialTheme {
                SettingsScreen(screenState = screenState.value, onEvent = { events += it }, prefs = prefs)
            }
        }
    }

    @After
    fun closeScreen() {
        scope.cancel()
    }

    /** Rebuilds the items from the current preferences, as the view model does whenever they change. */
    protected fun render(inProgress: Boolean = false) {
        items = preferenceItems(prefs, inProgress, GooglePlayServices(context), context, DiagnosticStorageState())
        screenState.value = SettingsViewState(items, inProgress, notifications.areNotificationsEnabled)
        compose.waitForIdle()
    }

    protected fun item(key: String) = items.filterIsInstance<ActionablePreferenceItem>().single { it.key == key }

    protected fun titleOf(item: ActionablePreferenceItem) =
        if (item.titleRes != 0) context.getString(item.titleRes) else item.title

    protected fun summaryOf(item: ActionablePreferenceItem) =
        if (item.summaryRes != 0) context.getString(item.summaryRes) else item.summary

    protected fun scrollTo(title: String, ignoreCase: Boolean = false) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(title, ignoreCase = ignoreCase))
    }

    protected fun scrollToItem(key: String): String {
        val title = titleOf(item(key))
        scrollTo(title)
        return title
    }

    protected fun row(key: String): SemanticsNodeInteraction = compose.onNode(hasText(scrollToItem(key)))

    protected fun switchOf(key: String): SemanticsNodeInteraction =
        compose.onNode(isToggleable() and hasAnyAncestor(hasText(scrollToItem(key))))

    protected fun chip(label: String) = compose.onNode(
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and hasText(label)
    )

    protected fun assertSwitch(key: String, on: Boolean) {
        val switch = switchOf(key)
        if (on) switch.assertIsOn() else switch.assertIsOff()
    }

    protected fun assertSelectedChip(labels: Array<String>, selectedIndex: Int) {
        labels.forEachIndexed { index, label ->
            if (index == selectedIndex) chip(label).assertIsSelected() else chip(label).assertIsNotSelected()
        }
    }

    inline fun <reified T : SettingsViewEvent> onlyEvent(): T {
        val event = events.single()
        assertTrue("Unexpected event ${event::class.simpleName}", event is T)
        return event as T
    }
}