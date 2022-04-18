package com.anod.appwatcher.utils

import android.util.Log
import info.anodsplace.applog.AppLog
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

class AppLogLogger : Logger(convertLevel(AppLog.level)) {

    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> AppLog.d(msg)
            Level.INFO -> AppLog.i(msg)
            Level.ERROR -> AppLog.e(msg)
            else -> AppLog.e(msg)
        }
    }

    companion object {
        fun convertLevel(appLogLevel: Int) : Level {
            return when (appLogLevel) {
                Log.DEBUG -> Level.DEBUG
                Log.INFO -> Level.INFO
                Log.ERROR -> Level.ERROR
                Log.VERBOSE -> Level.DEBUG
                Log.ASSERT -> Level.ERROR
                Log.WARN -> Level.ERROR
                else -> Level.ERROR
            }
        }
    }
}