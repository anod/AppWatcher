package com.anod.appwatcher.diagnostics

import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException

data class DiagnosticStorageState(
    val bytes: Long = 0,
    val oldest: Long? = null,
    val dropped: Long = 0,
    val writeFailures: Long = 0,
    val error: Boolean = false
)

/** Access only from the recorder's serialized IO consumer. Segments are evicted oldest first. */
class DiagnosticFileStore(
    private val directory: File,
    private val maxBytes: Long = MAX_BYTES,
    private val segmentBytes: Long = 256L * 1024,
    private val retentionMillis: Long = RETENTION_MILLIS
) {
    private val segmentLimit = segmentBytes.coerceAtMost(maxBytes)
    private var active: File? = null
    private var output: BufferedOutputStream? = null
    private var activeBytes = 0L
    private var initialized = false

    fun append(line: String, now: Long) {
        val bytes = line.toByteArray(Charsets.UTF_8)
        require(bytes.size <= segmentLimit)
        if (!initialized) {
            prune(now, 0)
            initialized = true
        }
        if (active == null || activeBytes + bytes.size > segmentLimit) {
            close()
            prune(now, segmentLimit)
            if (!directory.isDirectory && !directory.mkdirs()) throw IOException("diagnostic directory")
            val sequence = (files().lastOrNull()?.name?.substringBefore('-')?.toLongOrNull() ?: -1L) + 1
            active = File(directory, "${sequence.toString().padStart(20, '0')}-$now.log")
            output = BufferedOutputStream(active!!.outputStream())
            activeBytes = 0
        }
        output!!.write(bytes)
        activeBytes += bytes.size
    }

    fun flush() {
        output?.flush()
    }

    fun close() {
        output?.close()
        output = null
        active = null
        activeBytes = 0
    }

    fun state(now: Long): DiagnosticStorageState {
        prune(now, 0)
        val files = files()
        return DiagnosticStorageState(
            bytes = files.sumOf { it.length() },
            oldest = files.firstOrNull()?.name?.substringAfter('-')?.substringBefore('.')?.toLongOrNull()
        )
    }

    fun snapshot(destination: File, now: Long): File {
        output?.flush()
        prune(now, 0)
        if (!destination.parentFile!!.isDirectory && !destination.parentFile!!.mkdirs()) throw IOException("diagnostic export directory")
        try {
            destination.outputStream().use { output ->
                files().forEach { file -> file.inputStream().use { it.copyTo(output) } }
            }
        } catch (e: Exception) {
            destination.delete()
            throw e
        }
        return destination
    }

    fun delete() {
        close()
        files().forEach { if (!it.delete()) throw IOException("diagnostic delete") }
        active = null
    }

    private fun files(): List<File> {
        if (!directory.exists()) return emptyList()
        return (directory.listFiles() ?: throw IOException("diagnostic list"))
            .filter { it.isFile && it.extension in listOf("log", "jsonl") }
            .sortedBy { it.name }
    }

    private fun prune(now: Long, incoming: Long) {
        output?.flush()
        val files = files()
        var total = files.sumOf { it.length() }
        for (file in files) {
            val created = file.name.substringAfter('-').substringBefore('.').toLongOrNull() ?: 0L
            if (created < now - retentionMillis || total + incoming > maxBytes) {
                if (active == file) close()
                val size = file.length()
                if (!file.delete()) throw IOException("diagnostic prune")
                total -= size
                if (active == file) active = null
            }
        }
    }

    companion object {
        const val MAX_BYTES = 10L * 1024 * 1024
        const val RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}