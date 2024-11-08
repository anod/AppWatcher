package com.anod.appwatcher.database

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.BuildConfig
import info.anodsplace.applog.AppLog

class DbContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID

        private const val APPS = 10
        private const val APP = 20
        private const val CHANGELOG_VERSION = 200

        val appsUri = Uri.parse("content://$AUTHORITY/apps")!!
        val changelogUri = Uri.parse("content://$AUTHORITY/changelog")!!

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(AUTHORITY, "apps", APPS)
            uriMatcher.addURI(AUTHORITY, "apps/#", APP)

            uriMatcher.addURI(AUTHORITY, "changelog/apps/*/v/#", CHANGELOG_VERSION)
        }
    }

    private val dbSchemaManager: DbDataSource by lazy {
        DbDataSource((context as AppWatcherApplication).appsDatabase.openHelper)
    }

    private fun matchQuery(uri: Uri): Query? {
        val matched = uriMatcher.match(uri)
        if (matched == -1) {
            return null
        }
        when (matched) {
            APP -> {
                val rowId = uri.lastPathSegment ?: "0"
                return Query(
                    type = matched,
                    table = AppListTable.TABLE,
                    selection = BaseColumns._ID + "=?",
                    selectionArgs = arrayOf(rowId),
                    notifyUri = appsUri.buildUpon().appendPath(rowId).build()
                )
            }

            CHANGELOG_VERSION -> {
                return Query(
                    type = matched,
                    table = ChangelogTable.TABLE,
                    selection = ChangelogTable.Columns.APP_ID + "=? AND " + ChangelogTable.Columns.VERSION_CODE + "=?",
                    selectionArgs = arrayOf(
                        uri.pathSegments[uri.pathSegments.size - 3],
                        uri.lastPathSegment ?: "-1"
                    ),
                    notifyUri = changelogUri
                )
            }
        }
        return null
    }

    private class Query(
        val type: Int,
        val table: String,
        val selection: String,
        val selectionArgs: Array<String>,
        val notifyUri: Uri? = null,
        val groupBy: String = "",
        val projection: Array<String> = emptyArray(),
    )

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalArgumentException("Unknown URI $uri")
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI $uri")
        require(!(values == null || values.size() == 0)) { "Values cannot be empty" }

        val db = dbSchemaManager.writableDatabase
        val rowId = db.insert(query.table, SQLiteDatabase.CONFLICT_REPLACE, values)
        if (rowId > 0 && query.notifyUri != null) {
            val noteUri = ContentUris.withAppendedId(query.notifyUri, rowId)
            context!!.contentResolver.notifyChange(noteUri, null)
            return noteUri
        }
        throw SQLException("Failed to insert row into $uri")
    }

    override fun onCreate(): Boolean {
        AppLog.d("Initializing ${(context as AppWatcherApplication).appsDatabase.openHelper.databaseName}")
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): AppListCursor? {
        var proj = projection
        var sel = selection
        var selArgs = selectionArgs ?: emptyArray()
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI $uri")
// Using SQLiteQueryBuilder instead of queryApps() method
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = query.table

        if (selection == null) {
            sel = query.selection
            selArgs = query.selectionArgs
        }
        if (projection == null) {
            proj = query.projection
        }

        val db = dbSchemaManager.writableDatabase
        val sqlQuery = queryBuilder.buildQuery(proj, sel, query.groupBy, null, sortOrder, null)
        val cursor = db.query(sqlQuery, selArgs)

// Make sure that potential listeners are getting notified
        cursor.setNotificationUri(context?.contentResolver, uri)
        return AppListCursor(cursor)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI $uri")
        require(!(values == null || values.size() == 0)) { "Values cannot be empty" }

        val db = dbSchemaManager.writableDatabase
        val count = db.update(query.table, SQLiteDatabase.CONFLICT_REPLACE, values, query.selection, query.selectionArgs)
        if (count > 0 && query.notifyUri != null) {
            context?.contentResolver?.notifyChange(query.notifyUri, null)
        }
        return count
    }
}