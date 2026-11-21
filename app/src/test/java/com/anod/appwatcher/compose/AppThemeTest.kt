package com.anod.appwatcher.compose

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.preferences.TestNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Shows what every entry of the Settings theme picker looks like, by day and at night. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35], qualifiers = "w411dp-h891dp")
class AppThemeTest {
    @get:Rule
    val compose = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val prefs = Preferences(context, TestNotificationManager(), scope, DiagnosticAllowlist(emptySet()))
    private val selectedTheme = mutableStateOf(prefs.selectedTheme)
    private val systemNight = mutableStateOf(false)
    private var rendered: ColorScheme? = null

    private val light get() = dynamicLightColorScheme(context)
    private val dark get() = dynamicDarkColorScheme(context)
    private val black get() = dark.copy(background = Color.Black, surface = Color.Black)

    @Before
    fun showThemedContent() {
        compose.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                val nightMode = if (systemNight.value) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                AppTheme(theme = selectedTheme.value, updateSystemBars = false) {
                    val colors = MaterialTheme.colorScheme.copy()
                    SideEffect { rendered = colors }
                }
            }
        }
    }

    @After
    fun closeScreen() {
        scope.cancel()
    }

    @Test
    fun lightDarkAndBlackLookDifferent() {
        assertNotEquals(light.background, dark.background)
        assertNotEquals(light.primary, dark.primary)
        assertNotEquals(Color.Black, dark.background)
        assertEquals(Color.Black, black.background)
    }

    @Test
    fun followSystemIsLightByDayAndDarkAtNight() {
        assertLooksLike(light, themeIndex = FOLLOW_SYSTEM, night = false)
        assertLooksLike(dark, themeIndex = FOLLOW_SYSTEM, night = true)
    }

    @Test
    fun followSystemWithBlackIsLightByDayAndPitchBlackAtNight() {
        assertLooksLike(light, themeIndex = FOLLOW_SYSTEM_BLACK, night = false)
        assertLooksLike(black, themeIndex = FOLLOW_SYSTEM_BLACK, night = true)
    }

    @Test
    fun lightIgnoresTheSystemDarkMode() {
        assertLooksLike(light, themeIndex = LIGHT, night = false)
        assertLooksLike(light, themeIndex = LIGHT, night = true)
    }

    @Test
    fun darkIgnoresTheSystemLightMode() {
        assertLooksLike(dark, themeIndex = DARK, night = false)
        assertLooksLike(dark, themeIndex = DARK, night = true)
    }

    @Test
    fun blackIsPitchBlackWhateverTheSystemMode() {
        assertLooksLike(black, themeIndex = BLACK, night = false)
        assertLooksLike(black, themeIndex = BLACK, night = true)
    }

    @Test
    fun switchingThemesWhileShownRestylesTheScreen() {
        assertLooksLike(light, themeIndex = LIGHT, night = true)
        assertLooksLike(black, themeIndex = BLACK, night = true)
        assertLooksLike(dark, themeIndex = DARK, night = true)
        assertLooksLike(dark, themeIndex = FOLLOW_SYSTEM, night = true)
        assertLooksLike(light, themeIndex = FOLLOW_SYSTEM, night = false)
    }

    private fun assertLooksLike(expected: ColorScheme, themeIndex: Int, night: Boolean) {
        prefs.themeIndex = themeIndex
        selectedTheme.value = prefs.selectedTheme
        systemNight.value = night
        compose.waitForIdle()

        val actual = checkNotNull(rendered)
        val description = "theme $themeIndex, system night mode $night"
        assertEquals("background of $description", expected.background, actual.background)
        assertEquals("surface of $description", expected.surface, actual.surface)
        assertEquals("primary of $description", expected.primary, actual.primary)
        assertEquals("onSurface of $description", expected.onSurface, actual.onSurface)
    }

    private companion object {
        const val FOLLOW_SYSTEM = 0
        const val FOLLOW_SYSTEM_BLACK = 1
        const val LIGHT = 2
        const val DARK = 3
        const val BLACK = 4
    }
}