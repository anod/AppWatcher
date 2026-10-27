package com.anod.appwatcher.diagnostics

import java.security.MessageDigest
import java.time.Instant
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

enum class DiagnosticEvent {
    PROCESS_START,
    PROCESS_EXIT,
    RECOVERY,
    POWER_STATE,
    WORK_SNAPSHOT,
    SNAPSHOT_UNAVAILABLE,
    SCHEDULE,
    SCHEDULE_RESULT,
    CANCEL_SCHEDULE,
    WORK_START,
    WORK_END,
    WORK_CANCELLED,
    WORK_FAILED,
    LOCK_WAIT,
    LOCK_ACQUIRED,
    LOCK_RELEASED,
    STAGE,
    SYNC_RESULT,
    FAILURE,
    NOTIFICATION,
    RECORDER_HEALTH
}

enum class DiagnosticStage {
    STARTED,
    SESSION,
    AUTH_TOKEN,
    DEVICE_REGISTRATION,
    INSTALLED_PACKAGES,
    LOAD_APPS,
    BULK_DETAILS,
    RELEASE_DETAILS,
    APPLY_UPDATES,
    SAVE_RESULT,
    NOTIFY,
    DRIVE_SYNC,
    CLEANUP,
    OTHER;

    companion object {
        fun fromInternalStage(stage: String): DiagnosticStage = when (stage.substringBefore(' ')) {
            "started" -> STARTED
            "session" -> SESSION
            "auth-token" -> AUTH_TOKEN
            "device-registration" -> DEVICE_REGISTRATION
            "installed-packages" -> INSTALLED_PACKAGES
            "load-apps" -> LOAD_APPS
            "bulk-details" -> BULK_DETAILS
            "release-details" -> RELEASE_DETAILS
            "apply-updates" -> APPLY_UPDATES
            "save-result" -> SAVE_RESULT
            "notify" -> NOTIFY
            "drive-sync" -> DRIVE_SYNC
            "cleanup" -> CLEANUP
            else -> OTHER
        }
    }
}

enum class DiagnosticDecision {
    MANUAL,
    PERIODIC_KEEP,
    PERIODIC_UPDATE,
    SUCCESS,
    IN_PROGRESS,
    FAILED,
    EXPECTED_TRANSIENT,
    UNEXPECTED,
    MANUAL_CANCEL,
    CANCEL_REQUESTED,
    NO_UPDATES,
    FILTERED,
    POST_REQUESTED
}

class DiagnosticRun(val id: UUID) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<DiagnosticRun>
}

// No free text, exception, account, package name, or network payload is accepted by this API.
data class DiagnosticRecord(
    val event: DiagnosticEvent,
    val run: UUID? = null,
    val stage: DiagnosticStage? = null,
    val decision: DiagnosticDecision? = null,
    val count: Long? = null,
    val secondaryCount: Long? = null,
    val code: Int? = null,
    val attempt: Int? = null,
    val flag: Boolean? = null,
    val secondaryFlag: Boolean? = null,
    val historicalTime: Long? = null
) {
    fun encode(time: Long, process: UUID): String = buildString {
        append("{\"time\":\"${Instant.ofEpochMilli(time)}\",\"process\":\"$process\",\"event\":\"${event.name}\"")
        run?.let { append(",\"run\":\"$it\"") }
        stage?.let { append(",\"stage\":\"${it.name}\"") }
        decision?.let { append(",\"decision\":\"${it.name}\"") }
        count?.let { append(",\"count\":$it") }
        secondaryCount?.let { append(",\"secondaryCount\":$it") }
        code?.let { append(",\"code\":$it") }
        attempt?.let { append(",\"attempt\":$it") }
        flag?.let { append(",\"flag\":$it") }
        secondaryFlag?.let { append(",\"secondaryFlag\":$it") }
        historicalTime?.let { append(",\"historicalTime\":$it") }
        append("}\n")
    }
}

object DiagnosticAccounts {
    fun hash(account: String): String = MessageDigest.getInstance("SHA-256")
        .digest(account.trim().lowercase(Locale.ROOT).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    fun allows(account: String?, hashes: String): Boolean =
        !account.isNullOrBlank() && hash(account) in hashes.split(',').filter { it.length == 64 }
}