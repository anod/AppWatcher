package com.anod.appwatcher.userLog

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.squareup.tape2.ObjectQueue
import com.squareup.tape2.QueueFile
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Alex Gavrishev
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
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)

        fun from(bytes: ByteArray): UserLogMessage {
            val str = String(bytes, Charsets.UTF_8)

            val levelIndex = str.indexOf('[')
            val date = str.substring(0, levelIndex)
            val level = str[levelIndex + 1].toInt() - '0'.toInt()
            val message = str.substring(levelIndex + 3)
            return UserLogMessage(format.parse(date), message, level)
        }
    }

    override val asBytes: ByteArray
        get() = toString().toByteArray(Charsets.UTF_8)

    override fun toString(): String {
        return "${format.format(date)}[$level]$message"
    }
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
    private val handlerThread = HandlerThread("UserLogger")
    private val handler: Handler by lazy {
        Handler(handlerThread.looper) {
            when (it.what) {
                messageRemove -> {
                    objectQueue.remove(it.arg1); true
                }
                messageLog -> {
                    objectQueue.add(it.obj as UserLogMessage);true
                }
                else -> true
            }
        }
    }

    init {
        handlerThread.start()
    }

    companion object {
        const val messageRemove = 1
        const val messageLog = 2
    }

    private val objectQueue = ObjectQueue.create(queueFile, MessageConverter())

    fun info(message: String) {
        this.log(Log.INFO, message)
    }

    fun error(message: String) {
        this.log(Log.ERROR, message)
    }

    fun log(level: Int, message: String) {
        val userMessage = UserLogMessage(Calendar.getInstance().time, message, level)
        val handlerMessage = handler.obtainMessage(messageLog, userMessage)
        handler.sendMessage(handlerMessage)
    }

    val count: Int
        get() = objectQueue.size()

    val iterator: MutableIterator<Message>
        get() = objectQueue.iterator()

    fun remove(n: Int) {
        val message = handler.obtainMessage(messageRemove, n)
        handler.sendMessage(message)
    }

    val content: String
        get() = objectQueue.asList().joinToString("\n")
}

