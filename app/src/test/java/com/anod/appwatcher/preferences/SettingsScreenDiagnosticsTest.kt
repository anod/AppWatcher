package com.anod.appwatcher.preferences

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.R
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
    private val prefs = Preferences(context, NotificationManager.NoOp(), CoroutineScope(Dispatchers.Unconfined), "")

    private fun items() = listOf<PreferenceItem>(
        PreferenceItem.Switch(checked = false, titleRes = R.string.diagnostics_record, key = "diagnostics-enabled"),
        PreferenceItem.Text(titleRes = R.string.diagnostics_share, key = "diagnostics-share"),
        PreferenceItem.Text(titleRes = R.string.diagnostics_delete, key = "diagnostics-delete")
    )

    @Test
    fun renderingAndRecompositionNeverDispatchDiagnosticActionsButEachClickDispatchesOnce() {
        val events = mutableListOf<SettingsViewEvent>()
        val state = mutableStateOf(SettingsViewState(items()))
        compose.setContent {
            MaterialTheme {
                SettingsScreen(screenState = state.value, onEvent = { events += it }, prefs = prefs)
            }
        }
        compose.waitForIdle()
        state.value = SettingsViewState(items())
        compose.waitForIdle()
        assertTrue(events.toString(), events.isEmpty())

        compose.onNodeWithText(context.getString(R.string.diagnostics_record)).performClick()
        compose.waitForIdle()
        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.DiagnosticToggle(true)), events)
        events.clear()
        compose.onNodeWithText(context.getString(R.string.diagnostics_share)).performClick()
        compose.waitForIdle()
        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.DiagnosticShare), events)
        events.clear()
        compose.onNodeWithText(context.getString(R.string.diagnostics_delete)).performClick()
        compose.waitForIdle()
        assertEquals(listOf<SettingsViewEvent>(SettingsViewEvent.DiagnosticDelete), events)
    }
}