package com.anod.appwatcher.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTextInput
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.tags.EditTagScreen
import com.anod.appwatcher.tags.EditTagState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards against a Material3/compose-foundation ABI skew reaching a release.
 *
 * Every Material3 [androidx.compose.material3.TextField] in the app crashed at measure time when
 * `play-services-oss-licenses` dragged in a material3 build compiled against a different
 * compose-foundation minor line. The failure is a runtime binary incompatibility: it compiles
 * cleanly and only surfaces once real Compose UI is composed and measured.
 *
 * The app has exactly two Material3 `TextField` call sites, both covered here:
 * `TopBarSearchField` (behind all six search bars) and the labelled field in `EditTagScreen`.
 * Both assert non-empty layout bounds, because measure is where the crash lives.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class Material3TextFieldSurfacesTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun topBarSearchFieldMeasuresAndAcceptsInput() {
        compose.setContent {
            var query by remember { mutableStateOf("") }
            AppTheme(updateSystemBars = false) {
                TopBarSearchField(
                    query = query,
                    onValueChange = { query = it }
                )
            }
        }

        awaitMeasuredTextField().assertAcceptsInput("gmail")
    }

    @Test
    fun editTagFieldMeasuresAndAcceptsInput() {
        compose.setContent {
            AppTheme(updateSystemBars = false) {
                EditTagScreen(
                    screenState = EditTagState(tag = Tag(id = 0, name = "", color = 0xFFE91E63.toInt())),
                    onEvent = { }
                )
            }
        }

        awaitMeasuredTextField().assertAcceptsInput("games")
    }

    private fun awaitMeasuredTextField(): SemanticsNodeInteraction {
        compose.waitForIdle()
        val field = compose.onNode(hasSetTextAction())
        val bounds = field.fetchSemanticsNode().boundsInRoot
        assertTrue("Text field was composed but never measured, bounds were $bounds", bounds.width > 0f && bounds.height > 0f)
        return field
    }

    private fun SemanticsNodeInteraction.assertAcceptsInput(input: String) {
        performTextInput(input)
        compose.waitForIdle()
        assert(hasText(input))
    }
}