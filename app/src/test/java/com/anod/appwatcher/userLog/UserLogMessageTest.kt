package com.anod.appwatcher.userLog

import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Test

class UserLogMessageTest {
    @Test
    fun diagnosticLineKeepsTimestampPriorityAndMessageWithoutTheLoggerTag() {
        val messages = UserLogMessage.fromDiagnosticLines(
            listOf(
                "2026-07-01T10:20:30.123Z 4/AppWatcher: MAIN  Sync started: account=a",
                "2026-07-01T10:20:31Z 6/AppWatcher: Sync failed",
                "2026-07-01T10:20:32.500Z 2/AppWatcher: "
            )
        )

        assertEquals(
            listOf(
                UserLogMessage("2026-07-01T10:20:30.123Z", "MAIN  Sync started: account=a", Log.INFO),
                UserLogMessage("2026-07-01T10:20:31Z", "Sync failed", Log.ERROR),
                UserLogMessage("2026-07-01T10:20:32.500Z", "", Log.VERBOSE)
            ),
            messages
        )
    }

    @Test
    fun linesWithoutHeaderContinueThePreviousMessageAndLeadingOrphansAreDropped() {
        val messages = UserLogMessage.fromDiagnosticLines(
            listOf(
                "\tat orphan.Frame(Orphan.kt:1)",
                "2026-07-01T10:20:30.123Z 6/AppWatcher: Sync failed",
                "java.io.IOException: boom",
                "\tat com.anod.appwatcher.Sync.run(Sync.kt:42)",
                "",
                "2026-07-01T10:20:31.000Z 4/AppWatcher: next"
            )
        )

        assertEquals(
            listOf(
                UserLogMessage(
                    "2026-07-01T10:20:30.123Z",
                    "Sync failed\njava.io.IOException: boom\n\tat com.anod.appwatcher.Sync.run(Sync.kt:42)",
                    Log.ERROR
                ),
                UserLogMessage("2026-07-01T10:20:31.000Z", "next", Log.INFO)
            ),
            messages
        )
    }

    @Test
    fun unicodeLineSeparatorsStayInsideTheMessage() {
        val messages = UserLogMessage.fromDiagnosticLines(listOf("2026-07-01T10:20:30Z 4/AppWatcher: a\u2028b\u0085c"))

        assertEquals(listOf("a\u2028b\u0085c"), messages.map { it.message })
    }

    @Test
    fun noStoredLinesProduceNoMessages() {
        assertEquals(emptyList<UserLogMessage>(), UserLogMessage.fromDiagnosticLines(emptyList()))
    }
}