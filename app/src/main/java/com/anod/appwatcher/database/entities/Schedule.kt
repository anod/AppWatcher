// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.database.entities

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.SchedulesTable

sealed class ScheduleResult
object New : ScheduleResult()
object Success : ScheduleResult()
class Skipped(val reason: Int) : ScheduleResult()
class Failed(val reason: Int) : ScheduleResult()

@Entity(tableName = SchedulesTable.TABLE)
data class Schedule(
    @PrimaryKey
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long,
    @ColumnInfo(name = SchedulesTable.Columns.START)
    val start: Long,
    @ColumnInfo(name = SchedulesTable.Columns.FINISH)
    val finish: Long,
    @ColumnInfo(name = SchedulesTable.Columns.REASON)
    val reason: Int,
    @ColumnInfo(name = SchedulesTable.Columns.RESULT)
    val result: Int,
    @ColumnInfo(name = SchedulesTable.Columns.CHECKED)
    val checked: Int,
    @ColumnInfo(name = SchedulesTable.Columns.FOUND)
    val found: Int,
    @ColumnInfo(name = SchedulesTable.Columns.UNAVAILABLE)
    val unavailable: Int,
    @ColumnInfo(name = SchedulesTable.Columns.NOTIFIED)
    val notified: Int
) {
    @Ignore
    constructor(isManual: Boolean)
    : this(-1L, System.currentTimeMillis(), 0, if (isManual) REASON_MANUAL else REASON_SCHEDULE, STATUS_NEW, 0, 0, 0, 0)

    fun finish(
        status: Int,
        checked: Int = 0,
        found: Int = 0,
        unavailable: Int = 0
    ): Schedule {
        return Schedule(
            id,
            start,
            System.currentTimeMillis(),
            reason,
            status,
            checked,
            found,
            unavailable,
            0
        )
    }

    fun result(): ScheduleResult = when (result) {
        STATUS_SUCCESS -> Success
        STATUS_NEW -> New
        STATUS_SKIPPED_MIN_TIME -> Skipped(result)
        STATUS_SKIPPED_NO_WIFI -> Skipped(result)
        STATUS_FAILED -> Failed(result)
        STATUS_FAILED_NO_ACCOUNT -> Failed(result)
        STATUS_FAILED_NO_TOKEN -> Failed(result)
        else -> New
    }

    companion object {
        const val STATUS_NEW = 0
        const val STATUS_SUCCESS = 1
        const val STATUS_SKIPPED_NO_WIFI = 3
        const val STATUS_SKIPPED_MIN_TIME = 4
        const val STATUS_FAILED = 2
        const val STATUS_FAILED_NO_ACCOUNT = 5
        const val STATUS_FAILED_NO_TOKEN = 6

        const val REASON_MANUAL = 1
        const val REASON_SCHEDULE = 2
    }
}