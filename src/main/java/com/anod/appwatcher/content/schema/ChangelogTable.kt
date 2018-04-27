package com.anod.appwatcher.content.schema

import android.content.ContentValues
import android.provider.BaseColumns
import com.anod.appwatcher.model.AppChange

/**
 * @author alex
 * *
 * @date 2015-03-01
 */
class ChangelogTable {

    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val versionCode = "code"
            const val versionName = "name"
            const val details = "details"
            const val uploadDate = "upload_date"
        }
    }

    object TableColumns {
        const val _ID = ChangelogTable.table + "." + BaseColumns._ID
        const val appId = ChangelogTable.table + ".app_id"
        const val versionCode = ChangelogTable.table + ".code"
        const val versionName = ChangelogTable.table + ".name"
        const val details = ChangelogTable.table + ".details"
        const val uploadDate = ChangelogTable.table + ".upload_date"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val versionCode = 2
        const val versionName = 3
        const val details = 4
        const val uploadDate = 5
    }

    companion object {

        const val table = "changelog"

        val projection = arrayOf(
                TableColumns._ID,
                TableColumns.appId,
                TableColumns.versionCode,
                TableColumns.versionName,
                TableColumns.details,
                TableColumns.uploadDate)

        const val sqlCreate =
                "CREATE TABLE " + table + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        ChangelogTable.Columns.appId + " TEXT not null," +
                        ChangelogTable.Columns.versionCode + " INTEGER," +
                        ChangelogTable.Columns.versionName + " TEXT not null," +
                        ChangelogTable.Columns.details + " TEXT not null," +
                        ChangelogTable.Columns.uploadDate + " TEXT not null," +
                        "UNIQUE(${ChangelogTable.Columns.appId}, ${ChangelogTable.Columns.versionCode}) ON CONFLICT REPLACE" +
                        ") "

    }
}

val AppChange.contentValues: ContentValues
    get() {
        val values = ContentValues()
        values.put(ChangelogTable.Columns.appId, appId)
        values.put(ChangelogTable.Columns.versionCode, versionCode)
        values.put(ChangelogTable.Columns.versionName, versionName)
        values.put(ChangelogTable.Columns.details, details)
        values.put(ChangelogTable.Columns.uploadDate, uploadDate)
        return values
    }