package com.anod.appwatcher.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.model.schema.ChangelogTable
import com.anod.appwatcher.model.schema.TagsTable

import info.anodsplace.android.log.AppLog

class DbSchemaManager(context: Context)
    : SQLiteOpenHelper(context, DbSchemaManager.dbName, null, DbSchemaManager.version) {

    companion object {
        private const val version = 10
        const val dbName = "app_watcher"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(AppListTable.sqlCreate)
        db.execSQL(TagsTable.sqlCreate)
        db.execSQL(AppTagsTable.sqlCreate)
        db.execSQL(ChangelogTable.sqlCreate)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        AppLog.v("Db upgrade from: $oldVersion to $newVersion")
        when (oldVersion) {
            1 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_TEXT + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE)
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            2, 3 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_TEXT + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE)
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            4 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE)
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            5 -> {
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            6, 7 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            8 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            10 -> {
                db.execSQL(ChangelogTable.sqlCreate)
            }
        }
    }

}
