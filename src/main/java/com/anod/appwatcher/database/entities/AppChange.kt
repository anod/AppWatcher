package com.anod.appwatcher.database.entities

import androidx.room.*
import android.provider.BaseColumns
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
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        val id: Int,
        @ColumnInfo(name = ChangelogTable.Columns.appId)
        val appId: String,
        @ColumnInfo(name = ChangelogTable.Columns.versionCode)
        val versionCode: Int,
        @ColumnInfo(name = ChangelogTable.Columns.versionName)
        val versionName: String,
        @ColumnInfo(name = ChangelogTable.Columns.details)
        val details: String,
        @ColumnInfo(name = ChangelogTable.Columns.uploadDate)
        val uploadDate: String) {

    @Ignore
    constructor(appId: String, versionCode: Int, versionName: String, details: String, uploadDate: String)
        : this(0, appId, versionCode, versionName, details, uploadDate)

    override fun equals(other: Any?): Boolean {
        other as? AppChange ?: return false
        return when {
            appId != other.appId -> false
            versionCode != other.versionCode -> false
            versionName != other.versionName -> false
            details != other.details -> false
            uploadDate != other.uploadDate -> false
            else -> true
        }
    }

    val isEmpty: Boolean
        get() = appId.isEmpty()
}
