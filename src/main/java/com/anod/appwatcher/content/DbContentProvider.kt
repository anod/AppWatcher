package com.anod.appwatcher.content

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns

import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.model.DbOpenHelper
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.model.schema.TagsTable

open class DbContentProvider : ContentProvider() {

    private var mDatabaseOpenHelper: DbOpenHelper? = null

    private fun matchQuery(uri: Uri): Query? {
        val matched = sURIMatcher.match(uri)
        if (matched == -1) {
            return null
        }
        val query = Query()
        query.type = matched
        val rowId: String
        when (matched) {
            APP_ROW -> {
                query.table = AppListTable.TABLE_NAME
                rowId = uri.lastPathSegment
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = APPS_CONTENT_URI
                return query
            }
            APP_LIST -> {
                query.table = AppListTable.TABLE_NAME
                query.notifyUri = APPS_CONTENT_URI
                return query
            }
            APPS_TAG -> {
                query.table = AppTagsTable.TABLE_NAME + ", " + AppListTable.TABLE_NAME
                val tagId = uri.lastPathSegment
                query.selection = AppTagsTable.TableColumns.TAGID + "=?"
                query.selectionArgs = arrayOf(tagId)
                query.notifyUri = APPS_CONTENT_URI
                return query
            }
            APP_TAGS -> {
                query.table = AppTagsTable.TABLE_NAME + ", " + AppListTable.TABLE_NAME
                rowId = uri.pathSegments[uri.pathSegments.size - 2]
                query.selection = AppListTable.TableColumns._ID + "=? AND " + AppListTable.TableColumns.APPID + "=" + AppTagsTable.TableColumns.APPID
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            APPS_TAGS_CLEAN -> {
                query.table = AppTagsTable.TABLE_NAME
                query.selection = AppTagsTable.TableColumns.APPID+" NOT IN (SELECT " + AppTagsTable.TableColumns.APPID + " FROM " + AppTagsTable.TABLE_NAME + ")"
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            TAG_LIST -> {
                query.table = TagsTable.TABLE_NAME
                query.notifyUri = TAGS_CONTENT_URI
                return query
            }
            TAG_ROW -> {
                query.table = TagsTable.TABLE_NAME
                rowId = uri.lastPathSegment
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = TAGS_CONTENT_URI
                return query
            }
            TAG_APPS -> {
                query.table = AppTagsTable.TABLE_NAME
                val tagId = uri.pathSegments[uri.pathSegments.size - 2]
                query.selection = AppTagsTable.Columns.TAGID + "=?"
                query.selectionArgs = arrayOf(tagId)
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            TAGS_APPS -> {
                query.table = AppTagsTable.TABLE_NAME
                query.notifyUri = APPS_TAG_CONTENT_URI
                return query
            }
            TAGS_APPS_COUNT -> {
                query.table = AppTagsTable.TABLE_NAME
                query.notifyUri = APPS_TAG_CONTENT_URI
                query.projection = arrayOf(AppTagsTable.Columns.TAGID, "count() as count")
                query.groupBy = AppTagsTable.Columns.TAGID
                return query
            }
            ICON_ROW -> {
                query.table = AppListTable.TABLE_NAME
                rowId = uri.lastPathSegment
                query.selection = BaseColumns._ID + "=?"
                query.selectionArgs = arrayOf(rowId)
                query.notifyUri = ICONS_CONTENT_URI
                return query
            }
        }
        return null
    }

    private class Query {
        internal var type: Int = 0
        internal var table: String? = null
        internal var selection: String? = null
        internal var selectionArgs: Array<String>? = null
        internal var notifyUri: Uri? = null
        internal var groupBy: String? = null
        internal var projection: Array<String>? = null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)

        val db = mDatabaseOpenHelper!!.writableDatabase
        val count: Int
        if (selection != null) {
            count = db.delete(query.table, selection, selectionArgs)
        } else {
            count = db.delete(query.table, query.selection, query.selectionArgs)
        }
        if (count > 0 && query.notifyUri != null) {
            context!!.contentResolver.notifyChange(query.notifyUri!!, null)
        }
        return count
    }


    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)
        if (values == null || values.size() == 0) {
            throw IllegalArgumentException("Values cannot be empty")
        }

        val db = mDatabaseOpenHelper!!.writableDatabase
        val rowId = db.insert(query.table, null, values)
        if (rowId > 0 && query.notifyUri != null) {
            val noteUri = ContentUris.withAppendedId(query.notifyUri, rowId)
            context!!.contentResolver.notifyChange(noteUri, null)
            return noteUri
        }
        throw SQLException("Failed to insert row into " + uri)
    }

    override fun onCreate(): Boolean {
        mDatabaseOpenHelper = DbOpenHelper(context)
        return false
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): AppListCursor? {
        var proj = projection
        var sel = selection
        var selArgs = selectionArgs
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)
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

        val db = mDatabaseOpenHelper!!.writableDatabase
        val cursor = queryBuilder.query(db, proj, sel, selArgs, query.groupBy, null, sortOrder, null) ?: return null

// Make sure that potential listeners are getting notified
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return AppListCursor(cursor)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val query = matchQuery(uri) ?: throw IllegalArgumentException("Unknown URI " + uri)
        if (values == null || values.size() == 0) {
            throw IllegalArgumentException("Values cannot be empty")
        }

        val db = mDatabaseOpenHelper!!.writableDatabase
        val count = db.update(query.table, values, query.selection, query.selectionArgs)
        if (count > 0 && query.notifyUri != null) {
            context!!.contentResolver.notifyChange(query.notifyUri!!, null)
        }
        return count
    }

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID

        private const val APP_LIST = 10
        private const val APP_ROW = 20
        private const val APP_TAGS = 100
        private const val APPS_TAG = 60
        private const val APPS_TAGS_CLEAN = 110

        private const val TAG_LIST = 30
        private const val TAG_ROW = 40
        private const val TAG_APPS = 50
        private const val TAGS_APPS = 80
        private const val TAGS_APPS_COUNT = 90

        private const val ICON_ROW = 70

        val APPS_CONTENT_URI = Uri.parse("content://$AUTHORITY/apps")!!
        val APPS_TAG_CONTENT_URI = Uri.parse("content://$AUTHORITY/apps/tags")!!
        val APPS_TAG_CLEAN_CONTENT_URI = Uri.parse("content://$AUTHORITY/apps/tags/clean")!!
        val TAGS_CONTENT_URI = Uri.parse("content://$AUTHORITY/tags")!!
        val TAGS_APPS_CONTENT_URI = Uri.parse("content://$AUTHORITY/tags/apps")!!
        val TAGS_APPS_COUNT_CONTENT_URI = Uri.parse("content://$AUTHORITY/tags/apps/count")!!
        val ICONS_CONTENT_URI = Uri.parse("content://$AUTHORITY/icons")!!

        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sURIMatcher.addURI(AUTHORITY, "apps", APP_LIST)
            sURIMatcher.addURI(AUTHORITY, "apps/#", APP_ROW)
            sURIMatcher.addURI(AUTHORITY, "apps/tags/#", APPS_TAG)
            sURIMatcher.addURI(AUTHORITY, "apps/tags/clean", APPS_TAGS_CLEAN)
            sURIMatcher.addURI(AUTHORITY, "apps/#/tags", APP_TAGS)

            sURIMatcher.addURI(AUTHORITY, "tags", TAG_LIST)
            sURIMatcher.addURI(AUTHORITY, "tags/#", TAG_ROW)
            sURIMatcher.addURI(AUTHORITY, "tags/#/apps", TAG_APPS)
            sURIMatcher.addURI(AUTHORITY, "tags/apps", TAGS_APPS)
            sURIMatcher.addURI(AUTHORITY, "tags/apps/count", TAGS_APPS_COUNT)

            sURIMatcher.addURI(AUTHORITY, "icons/#", ICON_ROW)
        }

        fun matchIconUri(uri: Uri): Boolean {
            return sURIMatcher.match(uri) == ICON_ROW
        }

        fun appsContentUri(tag: Tag?): Uri {
            return if (tag == null)
                DbContentProvider.APPS_CONTENT_URI
            else
                DbContentProvider.APPS_TAG_CONTENT_URI.buildUpon().appendPath(tag.id.toString()).build()
        }
    }
}
