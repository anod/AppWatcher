package com.anod.appwatcher.userLog

import android.util.Log
import com.squareup.tape2.ObjectQueue
import com.squareup.tape2.QueueFile
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author algavris
 * @date 03/01/2018
 */


interface Message {
    val date: Date
    val message: String
    val level: Int

    val asBytes: ByteArray
}

class UserLogMessage(override val date: Date, override val message: String, override val level: Int) : Message {

    companion object {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

        fun from(bytes: ByteArray): UserLogMessage {
            val str = String(bytes, Charsets.UTF_8)

            val levelIndex = str.indexOf('[')
            val date = str.substring(0, levelIndex)
            val level = str[levelIndex+1].toInt()
            val message = str.substring(levelIndex+2)
            return UserLogMessage(format.parse(date), message, level)
        }
    }

    override val asBytes: ByteArray
        get() = "${format.format(date)}[$level]$message".toByteArray(Charsets.UTF_8)

}

class MessageConverter : ObjectQueue.Converter<Message> {
    override fun from(bytes: ByteArray): Message {
        return UserLogMessage.from(bytes)
    }

    override fun toStream(o: Message, bytes: OutputStream) {
        bytes.write(o.asBytes)
    }
}

class UserLogger(queueFile: QueueFile) {
    private val objectQueue = ObjectQueue.create(queueFile, MessageConverter())

    fun info(message: String) {
        this.log(Log.INFO, message)
    }

    fun error(message: String) {
        this.log(Log.ERROR, message)
    }

    fun log(level: Int, message: String) {
        val userMessage = UserLogMessage(Calendar.getInstance().time, message, level)
        objectQueue.add(userMessage)
    }

    val count: Int
        get() = objectQueue.size()

    fun iterator(): MutableIterator<Message> {
        return objectQueue.iterator()
    }

    fun remove(n: Int) {
        objectQueue.remove(n)
    }
}

