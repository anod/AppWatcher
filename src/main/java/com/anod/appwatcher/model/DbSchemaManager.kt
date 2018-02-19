package com.anod.appwatcher.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.model.schema.ChangelogTable
import com.anod.appwatcher.model.schema.TagsTable

import info.anodsplace.framework.AppLog

class DbSchemaManager(context: Context)
    : SQLiteOpenHelper(context, DbSchemaManager.dbName, null, DbSchemaManager.version) {

    companion object {
        private const val version = 12
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
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.uploadDate + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.priceText + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.priceCurrency + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.priceMicros + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.uploadDate + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.detailsUrl + " TEXT")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.appId + " = " + AppListTable.Columns.packageName + "")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.detailsUrl + " = 'details?doc=' || " + AppListTable.Columns.packageName)
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.iconUrl + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.appType + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.refreshTimestamp + " INTEGER")
            }
            2, 3 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.priceText + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.priceCurrency + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.priceMicros + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.uploadDate + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.detailsUrl + " TEXT")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.appId + " = " + AppListTable.Columns.packageName + "")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.detailsUrl + " = 'details?doc=' || " + AppListTable.Columns.packageName)
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.iconUrl + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.appType + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.refreshTimestamp + " INTEGER")
            }
            4 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.uploadDate + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.detailsUrl + " TEXT")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.appId + " = " + AppListTable.Columns.packageName + "")
                db.execSQL("UPDATE " + AppListTable.table + " SET " + AppListTable.Columns.detailsUrl + " = 'details?doc=' || " + AppListTable.Columns.packageName)
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.iconUrl + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.appType + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.refreshTimestamp + " INTEGER")
            }
            5 -> {
                db.execSQL(TagsTable.sqlCreate)
                db.execSQL(AppTagsTable.sqlCreate)
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.iconUrl + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.appType + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.refreshTimestamp + " INTEGER")
            }
            6, 7 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.iconUrl + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.appType + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.refreshTimestamp + " INTEGER")
            }
            8 -> {
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.appType + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.table + " ADD COLUMN " + AppListTable.Columns.refreshTimestamp + " INTEGER")
            }
            9,10,11 -> {
                db.execSQL(ChangelogTable.sqlCreate)
            }
        }
    }

}
