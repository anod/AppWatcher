package info.anodsplace.framework

import android.os.Looper
import android.util.Log
import java.util.IllegalFormatException
import java.util.Locale

/**
 * @author alex
 * @date 2015-05-11
 */
class AppLog {
    var listener: Listener? = null

    interface Logger {
        fun println(priority: Int, tag: String, msg: String)

        open class Android : Logger {
            override fun println(priority: Int, tag: String, msg: String) {
                Log.println(priority, tag, msg)
            }
        }

        open class StdOut : Logger {
            override fun println(priority: Int, tag: String, msg: String) {
                println("[$tag:$priority] $msg")
            }
        }
    }

    interface Listener {
        fun onLogException(tr: Throwable)
    }

    companion object {

        var tag = "AppLog"
        var level = Log.INFO
        val instance: AppLog by lazy { AppLog() }

        val isVerbose = level <= Log.VERBOSE
        val isDebug = level <= Log.DEBUG

        var logger: Logger = Logger.Android()

        fun setDebug(buildConfigDebug: Boolean, loggableTag: String) {
            val isDebug = buildConfigDebug || Log.isLoggable(loggableTag, Log.DEBUG)
            if (isDebug) {
                level = Log.DEBUG
            } else {
                level = Log.INFO
            }
        }

        fun d(msg: String) {
            log(Log.DEBUG, format(msg))
        }

        fun d(msg: String, vararg params: Any) {
            log(Log.DEBUG, format(msg, *params))
        }

        fun v(msg: String) {
            log(Log.VERBOSE, format(msg))
        }

        fun e(msg: String) {
            loge(format(msg), null)
        }

        fun e(msg: String, tr: Throwable) {
            loge(format(msg), tr)
            instance.listener?.onLogException(tr)
        }

        fun e(tr: Throwable) {
            val message = tr.message ?: "Throwable is null"
            e(message, tr)
        }

        fun e(msg: String, vararg params: Any) {
            loge(format(msg, *params), null)
        }

        fun w(msg: String) {
            log(Log.VERBOSE, format(msg))
        }

        fun v(msg: String, vararg params: Any) {
            log(Log.VERBOSE, format(msg, *params))
        }

        private fun log(priority: Int, msg: String) {
            if (priority >= level) {
                logger.println(priority, tag, msg)
            }
        }

        private fun loge(message: String, tr: Throwable?) {
            val trace = if (logger is Logger.Android) Log.getStackTraceString(tr) else ""
            logger.println(Log.ERROR, tag, message + '\n'.toString() + trace)
        }

        private fun format(msg: String, vararg array: Any): String {
            var formatted: String
            if (array.isEmpty()) {
                formatted = msg
            } else {
                try {
                    formatted = String.format(Locale.US, msg, *array)
                } catch (ex: IllegalFormatException) {
                    e("IllegalFormatException: formatString='%s' numArgs=%d", msg, array.size)
                    formatted = msg + " (An error occurred while formatting the message.)"
                }
            }
            val stackTrace = Throwable().fillInStackTrace().stackTrace
            var string = "<unknown>"
            for (i in 2 until stackTrace.size) {
                val className = stackTrace[i].className
                if (className != AppLog::class.java.name) {
                    val substring = className.substring(1 + className.lastIndexOf(46.toChar()))
                    string = substring.substring(1 + substring.lastIndexOf(36.toChar())) + "." + stackTrace[i].methodName
                    break
                }
            }
            val isMain = Looper.myLooper() == Looper.getMainLooper()
            return String.format(Locale.US, "[%s%d] %s: %s", if (isMain) "MAIN:" else "", Thread.currentThread().id, string, formatted)
        }
    }
}
