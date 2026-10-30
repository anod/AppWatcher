package com.anod.appwatcher.preferences

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class SettingsScreenDiagnosticsTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs = Preferences(context, NotificationManager.NoOp(), CoroutineScope(Dispatchers.Unconfined), DiagnosticAllowlist(emptySet()))

    private fun items(summary: String) = listOf<PreferenceItem>(
        PreferenceItem.Switch(checked = false, titleRes = R.string.diagnostics_record, summary = summary, key = "diagnostics-enabled")
    )

    @Test
    fun renderingAndRecompositionNeverDispatchDiagnosticActionsButEachClickDispatchesOnce() {
        val events = mutableListOf<SettingsViewEvent>()
        val state = mutableStateOf(SettingsViewState(items("Size: 0 B")))
        compose.setContent {
            MaterialTheme {
                SettingsScreen(screenState = state.value, onEvent = { events += it }, prefs = prefs)
            }
        }
        compose.waitForIdle()
        state.value = SettingsViewState(items("Oldest (UTC): never\nSize: 2 KB"))
        compose.waitForIdle()
        assertTrue(events.toString(), events.isEmpty())
        compose.onNodeWithText("Oldest (UTC): never\nSize: 2 KB").assertIsDisplayed()

        compose.onNodeWithText(context.getString(R.string.diagnostics_record)).performClick()
        compose.waitForIdle()
        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.DiagnosticToggle(true)), events)
    }
}