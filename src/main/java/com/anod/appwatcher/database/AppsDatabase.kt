package com.anod.appwatcher.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.framework.AppLog

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */
@Database(
        entities = [(App::class), (AppChange::class), (AppTag::class), (Tag::class)],
        version = AppsDatabase.version,
        exportSchema = false)
abstract class AppsDatabase: RoomDatabase() {

    abstract fun apps(): AppListTable
    abstract fun changelog(): ChangelogTable
    abstract fun tags(): TagsTable
    abstract fun appTags(): AppTagsTable

    companion object {
        const val version = 14
        private const val dbName = "app_watcher"

        fun instance(context: Context): AppsDatabase {
            return Room.databaseBuilder(context, AppsDatabase::class.java, dbName)
                    .addMigrations(MIGRATION_13_14)
                    .build()
        }

        private val MIGRATION_13_14 = object: Migration(13,14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                AppLog.d("Migrate db from 13 to 14")
            }
        }
    }
}