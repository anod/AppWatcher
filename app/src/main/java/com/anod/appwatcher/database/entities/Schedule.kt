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

@Entity(tableName = SchedulesTable.table)
data class Schedule(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        var id: Long,
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
        val unavailable: Int
) {
    @Ignore
    constructor(isManual: Boolean)
            : this(-1L, System.currentTimeMillis(), 0, if (isManual) reasonManual else reasonSchedule, statusNew, 0, 0, 0)

    fun finish(status: Int, checked: Int = 0, found: Int = 0, unavailable: Int = 0): Schedule {
        return Schedule(
                id,
                start,
                System.currentTimeMillis(),
                reason,
                status,
                checked,
                found,
                unavailable
        )
    }

    fun result(): ScheduleResult = when (result) {
        statusSuccess -> Success
        statusNew -> New
        statusSkippedMinTime -> Skipped(result)
        statusSkippedNoWifi -> Skipped(result)
        statusFailed -> Failed(result)
        statusFailedNoAccount -> Failed(result)
        statusFailedNoToken -> Failed(result)
        else -> New
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