package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.BaseColumns

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */

@Entity(tableName = AppListTable.table)
data class App(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = BaseColumns._ID) val rowId: Int = 0,

    @ColumnInfo(name = AppListTable.Columns.appId) val appId: String,

    @ColumnInfo(name = AppListTable.Columns.packageName) val packageName: String,

    @ColumnInfo(name = AppListTable.Columns.versionNumber) val versionNumber: Int,

    @ColumnInfo(name = AppListTable.Columns.versionName) val versionName: String,

    @ColumnInfo(name = AppListTable.Columns.title) val title: String,

    @ColumnInfo(name = AppListTable.Columns.creator) val creator: String,

    @ColumnInfo(name = AppListTable.Columns.iconUrl) val iconUrl: String,

    @ColumnInfo(name = AppListTable.Columns.status) val status: Int,

    @ColumnInfo(name = AppListTable.Columns.uploadDate) val uploadDate: String,

    @Embedded val price: Price,

    @ColumnInfo(name = AppListTable.Columns.detailsUrl) val detailsUrl: String?,

    @ColumnInfo(name = AppListTable.Columns.uploadTimestamp) val uploadTime: Long,

    @ColumnInfo(name = AppListTable.Columns.appType) val appType: String,

    @ColumnInfo(name = AppListTable.Columns.syncTimestamp) val syncTime: Long
) {
    constructor(
        rowId: Int,
        packageName: String,
        versionCode: Int,
        versionName: String,
        title: String,
        iconUrl: String,
        status: Int,
        uploadDate: String,
        uploadTime: Long,
        syncTime: Long
    ) : this(
        rowId = rowId,
        appId = packageName,
        packageName = packageName,
        versionNumber = versionCode,
        versionName = versionName,
        title = title,
        creator = "",
        iconUrl = iconUrl,
        status = status,
        uploadDate = uploadDate,
        price = Price("", "", 0),
        detailsUrl = createDetailsUrl(packageName),
        uploadTime = uploadTime,
        appType = "",
        syncTime = syncTime
    )

    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_UPDATED = 1
        const val STATUS_DELETED = 2

        fun createDetailsUrl(packageName: String): String {
            return "details?doc=$packageName"
        }
    }
}