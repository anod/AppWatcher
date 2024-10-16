package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.BaseColumns
import com.anod.appwatcher.database.ChangelogTable

/**
 * @author Alex Gavrishev
 * @date 02/09/2017
 */
@Entity(tableName = ChangelogTable.table,
        indices = [(Index(
                value = arrayOf(ChangelogTable.Columns.appId, ChangelogTable.Columns.versionCode),
                unique = true))])
data class AppChange(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = BaseColumns._ID)
        val id: Int = 0,
        @ColumnInfo(name = ChangelogTable.Columns.appId)
        val appId: String,
        @ColumnInfo(name = ChangelogTable.Columns.versionCode)
        val versionCode: Int,
        @ColumnInfo(name = ChangelogTable.Columns.versionName)
        val versionName: String,
        @ColumnInfo(name = ChangelogTable.Columns.details)
        val details: String,
        @ColumnInfo(name = ChangelogTable.Columns.uploadDate)
        val uploadDate: String,
        @ColumnInfo(name = ChangelogTable.Columns.noNewDetails)
        val noNewDetails: Boolean) {

    @Ignore
    constructor(appId: String, versionCode: Int, versionName: String, details: String, uploadDate: String, noNewDetails: Boolean)
        : this(0, appId, versionCode, versionName, details, uploadDate, noNewDetails)

    override fun equals(other: Any?): Boolean {
        if (other !is AppChange) return false
        return when {
            appId != other.appId -> false
            versionCode != other.versionCode -> false
            versionName != other.versionName -> false
            details != other.details -> false
            uploadDate != other.uploadDate -> false
            noNewDetails != other.noNewDetails -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = appId.hashCode()
        result = 31 * result + versionCode
        result = 31 * result + versionName.hashCode()
        result = 31 * result + details.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + noNewDetails.hashCode()
        return result
    }

    val isEmpty: Boolean
        get() = appId.isEmpty()

    companion object {
        val empty = AppChange("", 0, "", "", "", false)
    }
}
