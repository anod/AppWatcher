package com.anod.appwatcher.utils

import info.anodsplace.applog.AppLog

object LogCat {

    fun read(): List<String> {
        val cmd = listOf("logcat", "-t", "4000", "-v", "time", "-b", "main", AppLog.tag + ":I", "*:S")
        val process = ProcessBuilder().command(cmd).redirectErrorStream(true).start()
        try {
            return process.inputStream.use {
                it.reader().buffered().lineSequence().toList()
            }
        } finally {
            process.destroy()
        }
    }

}