package com.anod.appwatcher.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.RawQuery
import android.content.ContentValues
import android.provider.BaseColumns
import android.text.TextUtils
import com.anod.appwatcher.database.entities.*
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.preferences.Preferences
import java.util.ArrayList
import java.util.concurrent.TimeUnit


@Dao
interface AppListTable {

    @RawQuery(observedEntities = [(App::class),(AppChange::class)])
    fun loadAppList(query: SupportSQLiteQuery): LiveData<List<AppListItem>>

    @Query("SELECT * FROM $table WHERE ${Columns.appId} == :appId")
    fun loadApp(appId: String): App?

    @Query("SELECT * FROM $table WHERE ${BaseColumns._ID} == :rowId")
    fun loadAppRow(rowId: Int): App?

    @Query("SELECT ${BaseColumns._ID}, ${Columns.packageName} FROM $table WHERE " +
            "CASE :includeDeleted WHEN 'true' THEN ${Columns.status} != ${AppInfoMetadata.STATUS_DELETED} END ")
    fun loadPackages(includeDeleted: Boolean): List<PackageRowPair>

    object Queries {

        fun loadAppList(sortId: Int, titleFilter: String, table: AppListTable): LiveData<List<AppListItem>> {
            return loadAppList(sortId, titleFilter, table)
        }

        fun loadAppList(sortId: Int, tag: Tag?, titleFilter: String, table: AppListTable): LiveData<List<AppListItem>> {
            val selection = createSelection(tag, titleFilter)

            val sql = "SELECT ${AppListTable.table}.*, ${ChangelogTable.TableColumns.details}, " +
                    "CASE WHEN ${Columns.updateTimestamp} > $recentTime THEN 1 ELSE 0 END ${Columns.recentFlag} " +
                    "FROM ${AppListTable.table} " +
                    "LEFT JOIN ${ChangelogTable.table} ON " +
                    "${TableColumns.appId} == ${ChangelogTable.TableColumns.appId} " +
                    "AND ${TableColumns.versionNumber} == ${ChangelogTable.TableColumns.versionCode} " +
                    "WHERE ${selection.first}" +
                    "ORDER BY ${createSortOrder(sortId)}"
            return table.loadAppList(SimpleSQLiteQuery(sql, selection.second))
        }

        private fun createSortOrder(sortId: Int): String {
            val filter = ArrayList<String>()
            filter.add(AppListTable.Columns.status + " DESC")
            filter.add(AppListTable.Columns.recentFlag + " DESC")
            when (sortId) {
                Preferences.SORT_NAME_DESC -> filter.add(AppListTable.Columns.title + " COLLATE NOCASE DESC")
                Preferences.SORT_DATE_ASC -> filter.add(AppListTable.Columns.uploadTimestamp + " ASC")
                Preferences.SORT_DATE_DESC -> filter.add(AppListTable.Columns.uploadTimestamp + " DESC")
                else -> filter.add(AppListTable.Columns.title + " COLLATE NOCASE ASC")
            }
            return TextUtils.join(", ", filter)
        }

        private fun createSelection(tag: Tag?, titleFilter: String): Pair<String, Array<String>> {
            val selc = ArrayList<String>(3)
            val args = ArrayList<String>(3)

            selc.add(AppListTable.Columns.status + " != ?")
            args.add(AppInfoMetadata.STATUS_DELETED.toString())

            if (tag != null) {
                selc.add(AppTagsTable.TableColumns.tagId + " = ?")
                args.add(tag.id.toString())
                selc.add(AppTagsTable.TableColumns.appId + " = " + AppListTable.TableColumns.appId)
            }

            if (!TextUtils.isEmpty(titleFilter)) {
                selc.add(AppListTable.Columns.title + " LIKE ?")
                args.add("%$titleFilter%")
            }

            return Pair(TextUtils.join(" AND ", selc), args.toTypedArray())
        }
    }


    class Columns : BaseColumns {
        companion object {
            const val appId = "app_id"
            const val packageName = "package"
            const val versionNumber = "ver_num"
            const val versionName = "ver_name"
            const val title = "title"
            const val creator = "creator"
            const val iconCache = "icon"
            const val iconUrl = "iconUrl"
            const val status = "status"
            const val uploadTimestamp = "update_date"
            const val priceText = "price_text"
            const val priceCurrency = "price_currency"
            const val priceMicros = "price_micros"
            const val uploadDate = "upload_date"
            const val detailsUrl = "details_url"
            const val appType = "app_type"
            const val updateTimestamp = "sync_version"
            const val recentFlag = "recent_flag"
        }
    }

    object TableColumns {
        const val _ID = table + "." + BaseColumns._ID
        const val appId = "$table.app_id"
        const val versionNumber = "$table.ver_num"
    }

    object Projection {
        const val _ID = 0
        const val appId = 1
        const val packageName = 2
        const val versionNumber = 3
        const val versionName = 4
        const val title = 5
        const val creator = 6
        const val status = 7
        const val uploadTime = 8
        const val priceText = 9
        const val priceCurrency = 10
        const val priceMicros = 11
        const val uploadDate = 12
        const val detailsUrl = 13
        const val iconUrl = 14
        const val appType = 15
        const val refreshTime = 16
        const val recentFlag = 17
    }

    companion object {

        const val table = "app_list"
        private const val recentDays: Long = 3

        val recentTime: Long
            get() {
                val timestamp = System.currentTimeMillis()
                // https://stackoverflow.com/questions/13892163/get-timestamp-for-start-of-day
                // val dayEnd = dayStart + 86399999
                val dayStart = timestamp - (timestamp%86400000)
                return dayStart - TimeUnit.DAYS.toMillis(recentDays)
            }

        val projection: Array<String>
            get() = projection(recentTime)

        private fun projection(recentTime: Long): Array<String> {
            return arrayOf(
                    TableColumns._ID,
                    TableColumns.appId,
                    Columns.packageName,
                    Columns.versionNumber,
                    Columns.versionName,
                    Columns.title,
                    Columns.creator,
                    Columns.status,
                    Columns.uploadTimestamp,
                    Columns.priceText,
                    Columns.priceCurrency,
                    Columns.priceMicros,
                    Columns.uploadDate,
                    Columns.detailsUrl,
                    Columns.iconUrl,
                    Columns.appType,
                    Columns.updateTimestamp,
                    "case " +
                            "when ${Columns.updateTimestamp} > $recentTime then 1 " +
                            "else 0 end ${Columns.recentFlag}")
        }

        const val sqlCreate =
                "CREATE TABLE " + table + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Columns.appId + " TEXT not null," +
                        Columns.packageName + " TEXT not null," +
                        Columns.versionNumber + " INTEGER," +
                        Columns.versionName + " TEXT," +
                        Columns.title + " TEXT not null," +
                        Columns.creator + " TEXT," +
                        Columns.status + " INTEGER," +
                        Columns.uploadTimestamp + " INTEGER," +
                        Columns.priceText + " TEXT," +
                        Columns.priceCurrency + " TEXT," +
                        Columns.priceMicros + " INTEGER," +
                        Columns.uploadDate + " TEXT," +
                        Columns.detailsUrl + " TEXT," +
                        Columns.iconUrl + " TEXT," +
                        Columns.appType + " TEXT," +
                        Columns.updateTimestamp + " INTEGER" +
                    ") "
    }
}

val AppInfo.contentValues: ContentValues
    get() {
        val values = ContentValues()

        if (rowId > 0) {
            values.put(BaseColumns._ID, rowId)
        }
        values.put(AppListTable.Columns.appId, appId)
        values.put(AppListTable.Columns.packageName, packageName)
        values.put(AppListTable.Columns.title, title)
        values.put(AppListTable.Columns.versionNumber, versionNumber)
        values.put(AppListTable.Columns.versionName, versionName)
        values.put(AppListTable.Columns.creator, creator)
        values.put(AppListTable.Columns.status, status)
        values.put(AppListTable.Columns.uploadDate, uploadDate)

        values.put(AppListTable.Columns.priceText, priceText)
        values.put(AppListTable.Columns.priceCurrency, priceCur)
        values.put(AppListTable.Columns.priceMicros, priceMicros)

        values.put(AppListTable.Columns.detailsUrl, detailsUrl)

        values.put(AppListTable.Columns.iconUrl, iconUrl)
        values.put(AppListTable.Columns.uploadTimestamp, uploadTime)

        values.put(AppListTable.Columns.appType, appType)
        values.put(AppListTable.Columns.updateTimestamp, updateTime)
        return values
    }
