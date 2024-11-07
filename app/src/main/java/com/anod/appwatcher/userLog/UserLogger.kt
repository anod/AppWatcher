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

data class UserLogMessage(
    override val timestamp: String,
    override val message: String,
    override val level: Int
) : Message {

    companion object {
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

    override fun toString(): String {
        return "$timestamp[$level]$message"
    }
}

class UserLogger {

    val messages: List<Message> by lazy { LogCat.read().map { UserLogMessage.from(it) } }

    val content: String
        get() = messages.joinToString("\n")
}