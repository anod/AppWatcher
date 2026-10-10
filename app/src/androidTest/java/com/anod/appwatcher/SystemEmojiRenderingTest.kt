package com.anod.appwatcher

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.emoji2.text.EmojiCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies Emoji2's default system-font path and the layout semantics used by content capture.
 * This is upgrade coverage, not a reproduction of the original metadata crash.
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 35)
class SystemEmojiRenderingTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun systemFontsBypassCompatProcessingAndSupportTextLayoutSemantics() {
        val currentText = mutableStateOf("Loading emoji metadata")
        compose.setContent { Text(currentText.value) }
        compose.waitUntil(60_000) {
            EmojiCompat.isConfigured() &&
                EmojiCompat.get().loadState in listOf(
                    EmojiCompat.LOAD_STATE_SUCCEEDED,
                    EmojiCompat.LOAD_STATE_FAILED
                )
        }
        val compat = EmojiCompat.get()
        assertEquals("The system-font path must initialize", EmojiCompat.LOAD_STATE_SUCCEEDED, compat.loadState)

        val codepoints = (0x2000..0x3300).toList() + (0x1F000..0x1FBFF).toList()
        val samples = codepoints.map { String(Character.toChars(it)) }
        repeat(3) {
            samples.forEach { sample ->
                assertSame(
                    sample,
                    compat.process(sample, 0, sample.length, Int.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL)
                )
                val emojiPresentation = sample + '\uFE0F'
                assertSame(
                    emojiPresentation,
                    compat.process(
                        emojiPresentation,
                        0,
                        emojiPresentation.length,
                        Int.MAX_VALUE,
                        EmojiCompat.REPLACE_STRATEGY_ALL
                    )
                )
            }
        }

        // GetTextLayoutResult is the same semantics entry point used by content capture.
        samples.chunked(128).forEach { batch ->
            val text = batch.joinToString(" ")
            compose.runOnIdle { currentText.value = text }
            compose.waitForIdle()
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(text).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                assertTrue(action(layouts))
            }
            assertTrue("Semantics must actually produce a layout", layouts.isNotEmpty())
        }
    }
}