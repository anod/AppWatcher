package com.anod.appwatcher.model.schema

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
        }
    }

    object TableColumns {
        val _ID = AppTagsTable.table + "." + BaseColumns._ID
        val appId = AppTagsTable.table + ".appId"
        val versionCode = AppTagsTable.table + ".code"
        val versionName = AppTagsTable.table + ".name"
        val details = AppTagsTable.table + ".details"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val versionCode = 2
        const val versionName = 3
        const val details = 4
    }

    companion object {

        const val table = "changelog"

        val projection = arrayOf(
                TableColumns._ID,
                TableColumns.appId,
                TableColumns.versionCode,
                TableColumns.versionName,
                TableColumns.details)

        val sqlCreate =
                "CREATE TABLE " + table + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        ChangelogTable.Columns.appId + " TEXT not null," +
                        ChangelogTable.Columns.versionCode + " INTEGER," +
                        ChangelogTable.Columns.versionName + " TEXT not null," +
                        ChangelogTable.Columns.details + " TEXT not null," +
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
        return values
    }