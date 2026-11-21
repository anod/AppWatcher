package com.anod.appwatcher.preferences

import android.app.Application
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.anod.appwatcher.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.annotation.Config

@Config(application = Application::class, sdk = [35], qualifiers = "w411dp-h891dp")
class SettingsScreenIconShapeTest : SettingsScreenTestBase() {
    private val bundledMasks = context.resources.getStringArray(R.array.adaptive_icon_style_paths_values).filter { it.isNotEmpty() }
    private val noneLabel = context.resources.getStringArray(R.array.adaptive_icon_style_names).first()

    // Shapes are the unlabeled buttons inside the list; the back arrow in the top bar is outside of it
    private val shapes = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button) and
        SemanticsMatcher.keyNotDefined(SemanticsProperties.Text) and
        hasAnyAncestor(hasScrollAction())

    private fun showIconStyle() {
        render()
        scrollToItem("icon-style")
    }

    @Test
    fun noneRestoresTheSystemShape() {
        showIconStyle()

        compose.onNodeWithText(noneLabel).performClick()

        assertEquals("", onlyEvent<SettingsViewEvent.UpdateIconsShape>().newPath)
    }

    @Test
    fun everyBundledShapeCanBePicked() {
        prefs.iconShape = ""
        showIconStyle()
        compose.onAllNodes(shapes).assertCountEquals(bundledMasks.size)

        bundledMasks.forEachIndexed { index, mask ->
            events.clear()
            // Once a shape is picked the selector offers the system shape in front of the bundled ones
            val systemShapes = compose.onAllNodes(shapes).fetchSemanticsNodes().size - bundledMasks.size

            compose.onAllNodes(shapes)[systemShapes + index].performClick()

            assertEquals(mask, onlyEvent<SettingsViewEvent.UpdateIconsShape>().newPath)
        }
    }

    @Test
    fun theSystemShapeIsOfferedFirstWhileAShapeIsPicked() {
        showIconStyle()

        compose.onAllNodes(shapes).assertCountEquals(bundledMasks.size + 1)[0].performClick()

        assertEquals(prefs.defaultSystemMask, onlyEvent<SettingsViewEvent.UpdateIconsShape>().newPath)
    }

    @Test
    fun bundledShapesStillWorkNextToTheSystemShape() {
        showIconStyle()

        compose.onAllNodes(shapes).assertCountEquals(bundledMasks.size + 1)[1 + bundledMasks.lastIndex].performClick()

        assertEquals(bundledMasks.last(), onlyEvent<SettingsViewEvent.UpdateIconsShape>().newPath)
    }
}