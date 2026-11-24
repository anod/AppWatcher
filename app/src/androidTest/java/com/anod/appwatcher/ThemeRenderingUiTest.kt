package com.anod.appwatcher

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.anod.appwatcher.preferences.Preferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Checks on a device that every choice of the theme setting repaints the open settings screen.
 *
 * The app starts in a contrasting theme and the choice is then picked in the screen itself, which has
 * to apply it in place without recreating the activity or restarting the process. The pixels of the
 * list are then read back: black has to be pure black, dark has to be dark without being black, and
 * light has to be light. Following the system resolves against the device's own night mode, so it is
 * dark or light with the device.
 */
@RunWith(Parameterized::class)
class ThemeRenderingUiTest(
    private val name: String,
    private val nightMode: Int,
    private val theme: Int,
    private val selectedIndex: Int
) {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val compose = createAndroidComposeRule<AppWatcherActivity>()
    private val support = AppWatcherUiTestSupport(compose)
    private val robot = SettingsScreenRobot(compose, support)
    private val startingTheme = if (selectedIndex == LIGHT_INDEX) {
        mapOf(Preferences.NIGHT_MODE to UiModeManager.MODE_NIGHT_YES, Preferences.THEME to Preferences.THEME_BLACK)
    } else {
        mapOf(Preferences.NIGHT_MODE to UiModeManager.MODE_NIGHT_NO, Preferences.THEME to Preferences.THEME_DEFAULT)
    }

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(IsolatedWorkManagerRule())
        .around(StableStartupPreferencesRule(startingTheme))
        .around(compose)

    @Before
    fun openSettings() {
        support.awaitAppReady()
        robot.open()
    }

    @Test
    fun choosingTheThemeRepaintsTheOpenSettingsScreen() {
        val activity = compose.activity
        val title = context.getString(R.string.pref_title_theme)
        val labels = context.resources.getStringArray(R.array.themes).toList()

        robot.select(title, labels[selectedIndex])

        robot.assertSelectedChip(title = title, labels = labels, selectedIndex = selectedIndex)
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        assertEquals("night mode of $name", nightMode, stored.getInt(Preferences.NIGHT_MODE, -1))
        assertEquals("theme id of $name", theme, stored.getInt(Preferences.THEME, -1))
        assertSame("Choosing $name recreated the activity", activity, compose.activity)
        assertFalse("Choosing $name closed the activity", activity.isFinishing)

        val systemIsDark = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = nightMode == UiModeManager.MODE_NIGHT_YES || (nightMode == UiModeManager.MODE_NIGHT_AUTO && systemIsDark)
        val isBlack = isDark && theme == Preferences.THEME_BLACK

        val background = dominantColor(compose.onNode(hasScrollAction()).captureToImage())
        val luminance = Color(background).luminance()
        val observed = "%s painted the list #%08X, luminance %.3f, while the system is %s".format(
            name, background, luminance, if (systemIsDark) "dark" else "light"
        )

        when {
            isBlack -> assertEquals(observed, BLACK, background)
            isDark -> assertTrue(observed, background != BLACK && luminance < DARK_LUMINANCE_LIMIT)
            else -> assertTrue(observed, luminance > LIGHT_LUMINANCE_LIMIT)
        }
    }

    /** The background fills most of the list, so the most common sampled colour is the one it is painted in. */
    private fun dominantColor(image: ImageBitmap): Int {
        val pixels = IntArray(image.width * image.height)
        image.readPixels(pixels)
        val counts = HashMap<Int, Int>()
        for (index in pixels.indices step SAMPLE_STRIDE) {
            counts[pixels[index]] = (counts[pixels[index]] ?: 0) + 1
        }
        return counts.maxBy { it.value }.key
    }

    companion object {
        private const val PREFS_NAME = "WatcherPrefs"
        private const val LIGHT_INDEX = 2
        private const val BLACK = 0xFF000000.toInt()
        private const val SAMPLE_STRIDE = 13
        private const val DARK_LUMINANCE_LIMIT = 0.15f
        private const val LIGHT_LUMINANCE_LIMIT = 0.6f

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun variants(): List<Array<Any>> = listOf(
            arrayOf<Any>("followSystemDark", UiModeManager.MODE_NIGHT_AUTO, Preferences.THEME_DEFAULT, 0),
            arrayOf<Any>("followSystemBlack", UiModeManager.MODE_NIGHT_AUTO, Preferences.THEME_BLACK, 1),
            arrayOf<Any>("light", UiModeManager.MODE_NIGHT_NO, Preferences.THEME_DEFAULT, 2),
            arrayOf<Any>("dark", UiModeManager.MODE_NIGHT_YES, Preferences.THEME_DEFAULT, 3),
            arrayOf<Any>("black", UiModeManager.MODE_NIGHT_YES, Preferences.THEME_BLACK, 4)
        )
    }
}