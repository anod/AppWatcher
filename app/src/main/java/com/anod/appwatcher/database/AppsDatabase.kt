package com.anod.appwatcher.database

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */
@Database(
    entities = [(App::class), (AppChange::class), (AppTag::class), (Tag::class), (Schedule::class)],
    version = AppsDatabase.VERSION,
    exportSchema = true)
abstract class AppsDatabase : RoomDatabase() {

    abstract fun apps(): AppListTable
    abstract fun changelog(): ChangelogTable
    abstract fun tags(): TagsTable
    abstract fun appTags(): AppTagsTable
    abstract fun schedules(): SchedulesTable

    suspend fun applyBatchUpdates(contentResolver: ContentResolver, values: List<ContentValues>, uriMapper: (ContentValues) -> Uri): Array<ContentProviderResult> = withContext(Dispatchers.IO) {
        val operations = values.map {
            ContentProviderOperation.newUpdate(uriMapper(it)).withValues(it).build()
        }

        return@withContext withTransaction {
            contentResolver.applyBatch(DbContentProvider.AUTHORITY, ArrayList(operations))
        }
    }

    suspend fun applyBatchInsert(contentResolver: ContentResolver, values: List<ContentValues>, uriMapper: (ContentValues) -> Uri): Array<ContentProviderResult> = withContext(Dispatchers.IO) {
        val operations = values.map {
            ContentProviderOperation.newInsert(uriMapper(it)).withValues(it).build()
        }

        return@withContext withTransaction {
            contentResolver.applyBatch(DbContentProvider.AUTHORITY, ArrayList(operations))
        }
    }

    companion object {
        const val VERSION = 19
        val dbName = if (BuildConfig.DEBUG) "app_watcher.db" else "app_watcher"

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ${SchedulesTable.TABLE} ADD COLUMN ${SchedulesTable.Columns.NOTIFIED} INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `${SchedulesTable.TABLE}` (" +
                        "`_id` INTEGER NOT NULL, " +
                        "`start` INTEGER NOT NULL, " +
                        "`finish` INTEGER NOT NULL, " +
                        "`result` INTEGER NOT NULL, " +
                        "`reason` INTEGER NOT NULL, " +
                        "`checked` INTEGER NOT NULL, " +
                        "`found` INTEGER NOT NULL, " +
                        "`unavailable` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`_id`))")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE " + ChangelogTable.TABLE + " ADD COLUMN " + ChangelogTable.Columns.NO_NEW_DETAILS + " INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM app_tags WHERE _ID NOT IN ( " +
                    "SELECT MAX(_ID) " +
                    "FROM app_tags " +
                    "GROUP BY app_id, tags_id)")
                db.execSQL("CREATE UNIQUE INDEX `index_app_tags_app_id_tags_id` ON `app_tags` (`app_id`, `tags_id`)")
            }
        }

        private val MIGRATION_9_11 = object : Migration(9, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `changelog` (" +
                    "`_id` INTEGER NOT NULL, " +
                    "`app_id` TEXT NOT NULL, " +
                    "`code` INTEGER NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`details` TEXT NOT NULL, " +
                    "`upload_date` TEXT NOT NULL, PRIMARY KEY(`_id`))")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE " + ChangelogTable.TABLE + " ADD COLUMN " + ChangelogTable.Columns.UPLOAD_DATE + " TEXT")
                } catch (e: Exception) {
                    AppLog.e(e)
                }
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE " + ChangelogTable.TABLE + " ADD COLUMN " + ChangelogTable.Columns.UPLOAD_DATE + " TEXT")
                } catch (e: Exception) {
                    AppLog.e(e)
                }
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                AppLog.e("Migrate db from 13 to 14")

                db.execSQL("UPDATE app_list SET update_date = 0 WHERE update_date IS NULL")
                db.execSQL("UPDATE app_list SET ver_name = '' WHERE ver_name IS NULL")
                db.execSQL("UPDATE app_list SET creator = '' WHERE creator IS NULL")
                db.execSQL("UPDATE app_list SET title = app_id WHERE title IS NULL")
                db.execSQL("UPDATE app_list SET iconUrl = '' WHERE iconUrl IS NULL")
                db.execSQL("UPDATE app_list SET upload_date = '' WHERE upload_date IS NULL")
                db.execSQL("UPDATE app_list SET app_type = '' WHERE app_type IS NULL")
                db.execSQL("UPDATE app_list SET price_text = '' WHERE price_text IS NULL")
                db.execSQL("UPDATE app_list SET price_currency = '' WHERE price_currency IS NULL")
                db.execSQL("UPDATE app_list SET price_micros = 0 WHERE price_micros IS NULL")
                db.execSQL("UPDATE app_list SET sync_version = 0 WHERE sync_version IS NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS `app_list_temp` " +
                    "(`_id` INTEGER NOT NULL, " +
                    "`app_id` TEXT NOT NULL, " +
                    "`package` TEXT NOT NULL, " +
                    "`ver_num` INTEGER NOT NULL, " +
                    "`ver_name` TEXT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`creator` TEXT NOT NULL, " +
                    "`iconUrl` TEXT NOT NULL, " +
                    "`status` INTEGER NOT NULL, " +
                    "`upload_date` TEXT NOT NULL, " +
                    "`details_url` TEXT, " +
                    "`update_date` INTEGER NOT NULL, " +
                    "`app_type` TEXT NOT NULL, " +
                    "`sync_version` INTEGER NOT NULL, " +
                    "`price_text` TEXT NOT NULL, " +
                    "`price_currency` TEXT NOT NULL, " +
                    "`price_micros` INTEGER, PRIMARY KEY(`_id`))")
                db.execSQL("INSERT INTO app_list_temp (" +
                    "_id, app_id, package, ver_num, ver_name, title, creator," +
                    "iconUrl, status, upload_date, details_url, update_date, app_type," +
                    "sync_version, price_text, price_currency, price_micros) " +
                    "SELECT _id, app_id, package, ver_num, ver_name, title, creator," +
                    "iconUrl, status, upload_date, details_url, update_date, app_type," +
                    "sync_version, price_text, price_currency, price_micros " +
                    "FROM app_list"
                )
                db.execSQL("DROP TABLE app_list")
                db.execSQL("ALTER TABLE app_list_temp RENAME TO app_list")

                db.execSQL("UPDATE changelog SET upload_date = '' WHERE upload_date IS NULL")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `changelog_temp` (" +
                        "`_id` INTEGER NOT NULL, " +
                        "`app_id` TEXT NOT NULL, " +
                        "`code` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`details` TEXT NOT NULL, " +
                        "`upload_date` TEXT NOT NULL, PRIMARY KEY(`_id`))"
                )
                db.execSQL(
                    "INSERT INTO changelog_temp (_id, app_id, code, name, details, upload_date)" +
                        " SELECT _id, app_id, code, name, details, upload_date FROM changelog"
                )
                db.execSQL("DROP TABLE changelog")
                db.execSQL("ALTER TABLE changelog_temp RENAME TO changelog")
                db.execSQL("CREATE UNIQUE INDEX `index_changelog_app_id_code` ON `changelog` (`app_id`, `code`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `app_tags_temp` (" +
                        "`_id` INTEGER NOT NULL, " +
                        "`app_id` TEXT NOT NULL, " +
                        "`tags_id` INTEGER NOT NULL, PRIMARY KEY(`_id`))"
                )
                db.execSQL("INSERT INTO app_tags_temp (_id, app_id, tags_id) SELECT _id, app_id, tags_id FROM app_tags")
                db.execSQL("DROP TABLE app_tags")
                db.execSQL("ALTER TABLE app_tags_temp RENAME TO app_tags")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tags_temp` (" +
                        "`_id` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`color` INTEGER NOT NULL, PRIMARY KEY(`_id`))"
                )
                db.execSQL("INSERT INTO tags_temp (_id, name, color) SELECT _id, name, color FROM tags")
                db.execSQL("DROP TABLE tags")
                db.execSQL("ALTER TABLE tags_temp RENAME TO tags")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                AppLog.d("Migrated db from 18 to 19")
            }
        }

        val migrations: Array<Migration> = arrayOf(
            MIGRATION_9_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19
        )
    }
}