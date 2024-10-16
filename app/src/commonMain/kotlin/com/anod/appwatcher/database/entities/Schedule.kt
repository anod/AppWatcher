// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.BaseColumns
import com.anod.appwatcher.database.SchedulesTable
import kotlinx.datetime.Clock

sealed interface ScheduleResult {
    data object New : ScheduleResult
    data object Success : ScheduleResult
    class Skipped(val reason: Int) : ScheduleResult
    class Failed(val reason: Int) : ScheduleResult
}

@Entity(tableName = SchedulesTable.table)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long = 0,
    @ColumnInfo(name = SchedulesTable.Columns.start)
    val start: Long,
    @ColumnInfo(name = SchedulesTable.Columns.finish)
    val finish: Long,
    @ColumnInfo(name = SchedulesTable.Columns.reason)
    val reason: Int,
    @ColumnInfo(name = SchedulesTable.Columns.result)
    val result: Int,
    @ColumnInfo(name = SchedulesTable.Columns.checked)
    val checked: Int,
    @ColumnInfo(name = SchedulesTable.Columns.found)
    val found: Int,
    @ColumnInfo(name = SchedulesTable.Columns.unavailable)
    val unavailable: Int,
    @ColumnInfo(name = SchedulesTable.Columns.notified)
    val notified: Int
) {
    @Ignore
    constructor(isManual: Boolean)
            : this(
        id = -1L,
        start = Clock.System.now().toEpochMilliseconds(),
        finish = 0,
        reason = if (isManual) reasonManual else reasonSchedule,
        result = statusNew,
        checked = 0,
        found = 0,
        unavailable = 0,
        notified = 0
    )

    fun finish(status: Int, checked: Int = 0, found: Int = 0, unavailable: Int = 0): Schedule {
        return Schedule(
            id = id,
            start = start,
            finish = Clock.System.now().toEpochMilliseconds(),
            reason = reason,
            result = status,
            checked = checked,
            found = found,
            unavailable = unavailable,
            notified = 0
        )
    }

    fun result(): ScheduleResult = when (result) {
        statusSuccess -> ScheduleResult.Success
        statusNew -> ScheduleResult.New
        statusSkippedMinTime -> ScheduleResult.Skipped(result)
        statusSkippedNoWifi -> ScheduleResult.Skipped(result)
        statusFailed -> ScheduleResult.Failed(result)
        statusFailedNoAccount -> ScheduleResult.Failed(result)
        statusFailedNoToken -> ScheduleResult.Failed(result)
        else -> ScheduleResult.New
    }

    companion object {
        const val statusNew = 0
        const val statusSuccess = 1
        const val statusSkippedNoWifi = 3
        const val statusSkippedMinTime = 4
        const val statusFailed = 2
        const val statusFailedNoAccount = 5
        const val statusFailedNoToken = 6

        const val reasonManual = 1
        const val reasonSchedule = 2
    }
}