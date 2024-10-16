package com.anod.appwatcher.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */
class DbDataSource(private val db: SupportSQLiteOpenHelper) : SupportSQLiteOpenHelper {
    override val databaseName: String?
        get() = db.databaseName
    override val readableDatabase: SupportSQLiteDatabase
        get() = db.readableDatabase
    override val writableDatabase: SupportSQLiteDatabase
        get() = db.writableDatabase

    override fun close() {
        db.close()
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        db.setWriteAheadLoggingEnabled(enabled)
    }
}