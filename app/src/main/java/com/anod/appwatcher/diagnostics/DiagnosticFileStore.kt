package com.anod.appwatcher.diagnostics

import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID

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
    private var active: File? = null

    fun append(line: String, now: Long) {
        val bytes = line.toByteArray(Charsets.UTF_8)
        require(bytes.size <= segmentBytes && bytes.size <= maxBytes)
        prune(now)
        var target = active
        if (target == null || !target.exists() || target.length() + bytes.size > segmentBytes) {
            if (!directory.isDirectory && !directory.mkdirs()) throw IOException("diagnostic directory")
            val sequence = (files().lastOrNull()?.name?.substringBefore('-')?.toLongOrNull() ?: -1L) + 1
            target = File(directory, "${sequence.toString().padStart(20, '0')}-$now.jsonl")
            active = target
        }
        target.appendBytes(bytes)
        prune(now)
    }

    fun state(now: Long): DiagnosticStorageState {
        prune(now)
        val files = files()
        return DiagnosticStorageState(
            bytes = files.sumOf { it.length() },
            oldest = files.firstOrNull()?.bufferedReader()?.use { reader ->
                reader.readLine()?.substringAfter("\"time\":\"", "")?.substringBefore('"')
                    ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            }
        )
    }

    /** Counts work started by previous processes; [currentProcess] may still be running. */
    fun unfinishedRuns(now: Long, currentProcess: UUID): Int {
        prune(now)
        val runs = mutableSetOf<String>()
        files().forEach { file ->
            file.forEachLine { line ->
                val run = line.substringAfter("\"run\":\"", "").substringBefore('"')
                if (run.isNotEmpty()) {
                    val process = line.substringAfter("\"process\":\"", "").substringBefore('"')
                    if (line.contains("\"event\":\"WORK_START\"") && process != currentProcess.toString()) runs.add(run)
                    if (listOf("WORK_END", "WORK_CANCELLED", "WORK_FAILED").any { line.contains("\"event\":\"$it\"") }) runs.remove(run)
                }
            }
        }
        return runs.size
    }

    fun snapshot(destination: File, now: Long): File {
        prune(now)
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
        files().forEach { if (!it.delete()) throw IOException("diagnostic delete") }
        active = null
    }

    private fun files(): List<File> {
        if (!directory.exists()) return emptyList()
        return (directory.listFiles() ?: throw IOException("diagnostic list"))
            .filter { it.isFile && it.extension == "jsonl" }
            .sortedBy { it.name }
    }

    private fun prune(now: Long) {
        val files = files()
        var total = files.sumOf { it.length() }
        for (file in files) {
            val created = file.name.substringAfter('-').substringBefore('.').toLongOrNull() ?: 0L
            if (created < now - retentionMillis || total > maxBytes) {
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