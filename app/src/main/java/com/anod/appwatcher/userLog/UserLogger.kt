package com.anod.appwatcher.userLog

import android.util.Log
import com.anod.appwatcher.utils.LogCat

/**
 * @author Alex Gavrishev
 * @date 03/01/2018
 */

interface Message {
    val timestamp: String
    val message: String
    val level: Int
}

data class UserLogMessage(override val timestamp: String, override val message: String, override val level: Int) : Message {

    companion object {
        private val diagnosticLine = Regex("""(\d{4}-\d{2}-\d{2}T[\d:.]+Z) (\d{1,2})/.*?: (.*)""", RegexOption.DOT_MATCHES_ALL)

        /** Parses stored diagnostic lines. A line without a header continues the previous message, e.g. a stack trace. */
        fun fromDiagnosticLines(lines: List<String>): List<UserLogMessage> {
            val messages = mutableListOf<UserLogMessage>()
            for (line in lines) {
                val header = diagnosticLine.matchEntire(line)
                if (header != null) {
                    val (timestamp, priority, message) = header.destructured
                    messages.add(UserLogMessage(timestamp, message, priority.toInt()))
                } else if (messages.isNotEmpty()) {
                    val last = messages.last()
                    messages[messages.lastIndex] = last.copy(message = "${last.message}\n$line")
                }
            }
            return messages.map { it.copy(message = it.message.trimEnd()) }
        }

        fun from(logcatLine: String): UserLogMessage {
            val levelIndex = logcatLine.indexOf('/') - 1
            val messageIndex = logcatLine.indexOf(':', levelIndex)
            if (logcatLine.length < 22 || !logcatLine[0].isDigit() || levelIndex <= 0 || messageIndex <= 0) {
                return UserLogMessage("", logcatLine, Log.ERROR)
            }

            val level = logcatLine.substring(levelIndex, levelIndex + 1).map {
                when (it) {
                    'E' -> Log.ERROR
                    'W' -> Log.WARN
                    'I' -> Log.INFO
                    'V' -> Log.VERBOSE
                    else -> Log.DEBUG
                }
            }.first()

            val timestamp = logcatLine.substring(0, levelIndex - 1)
            val message = logcatLine.substring(messageIndex + 1)
            return UserLogMessage(timestamp, message, level)
        }
    }

    override fun toString(): String = "$timestamp[$level]$message"
}

class UserLogger {

    val messages: List<Message> by lazy { LogCat.read().map { UserLogMessage.from(it) } }

    val content: String
        get() = messages.joinToString("\n")
}