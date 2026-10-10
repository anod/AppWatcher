package com.anod.appwatcher.compose

import android.app.Application
import androidx.emoji2.text.EmojiCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class EmojiCompatDefaultsTest {

    @Test
    @Config(sdk = [34])
    fun android14StillLoadsCompatMetadata() {
        var loaderCalled = false
        val config = object : EmojiCompat.Config({
            loaderCalled = true
            it.onFailed(IllegalStateException("Test provider unavailable"))
        }) {}

        val compat = EmojiCompat.reset(config)

        assertTrue(loaderCalled)
        assertEquals(EmojiCompat.LOAD_STATE_FAILED, compat.loadState)
    }

    @Test
    @Config(sdk = [36])
    fun android15AndNewerSkipMetadataAndUseSystemFonts() {
        var loaderCalled = false
        val config = object : EmojiCompat.Config({
            loaderCalled = true
            it.onFailed(IllegalStateException("Test provider unavailable"))
        }) {}

        val compat = EmojiCompat.reset(config)
        val text = "\uD83D\uDE00 \u2764\uFE0F"

        assertFalse(loaderCalled)
        assertEquals(EmojiCompat.LOAD_STATE_SUCCEEDED, compat.loadState)
        assertSame(
            text,
            compat.process(text, 0, text.length, Int.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL)
        )
    }
}