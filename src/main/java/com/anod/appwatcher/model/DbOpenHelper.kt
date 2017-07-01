package com.anod.appwatcher.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.model.schema.TagsTable

import info.anodsplace.android.log.AppLog

class DbOpenHelper(context: Context)
    : SQLiteOpenHelper(context, DbOpenHelper.DATABASE_NAME, null, DbOpenHelper.DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(AppListTable.TABLE_CREATE)
        db.execSQL(TagsTable.TABLE_CREATE)
        db.execSQL(AppTagsTable.TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        AppLog.v("Db upgrade from: $oldVersion to $newVersion")
        when (oldVersion) {
            1 -> {
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_TEXT + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT")
                db.execSQL("UPDATE " + AppListTable.TABLE_NAME + " SET " + AppListTable.Columns.APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "")
                db.execSQL("UPDATE " + AppListTable.TABLE_NAME + " SET " + AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE)
                db.execSQL(TagsTable.TABLE_CREATE)
                db.execSQL(AppTagsTable.TABLE_CREATE)
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            2, 3 -> {
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_TEXT + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT")
                db.execSQL("UPDATE " + AppListTable.TABLE_NAME + " SET " + AppListTable.Columns.APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "")
                db.execSQL("UPDATE " + AppListTable.TABLE_NAME + " SET " + AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE)
                db.execSQL(TagsTable.TABLE_CREATE)
                db.execSQL(AppTagsTable.TABLE_CREATE)
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            4 -> {
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT")
                db.execSQL("UPDATE " + AppListTable.TABLE_NAME + " SET " + AppListTable.Columns.APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "")
                db.execSQL("UPDATE " + AppListTable.TABLE_NAME + " SET " + AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE)
                db.execSQL(TagsTable.TABLE_CREATE)
                db.execSQL(AppTagsTable.TABLE_CREATE)
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            5 -> {
                db.execSQL(TagsTable.TABLE_CREATE)
                db.execSQL(AppTagsTable.TABLE_CREATE)
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            6, 7 -> {
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_ICON_URL + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
            8 -> {
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_APP_TYPE + " TEXT")
                db.execSQL("ALTER TABLE " + AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_SYNC_VERSION + " INTEGER")
            }
        }
    }

    companion object {

        private const val DATABASE_VERSION = 9
        const val DATABASE_NAME = "app_watcher"
    }

}
