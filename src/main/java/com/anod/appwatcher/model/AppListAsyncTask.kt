package com.anod.appwatcher.model

import android.app.Application
import android.os.AsyncTask
import android.support.v4.content.ContentResolverCompat
import android.support.v4.os.CancellationSignal
import android.support.v4.os.OperationCanceledException
import android.text.TextUtils
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.database.FilterCursor
import info.anodsplace.framework.database.NullCursor
import java.util.*

/**
 * @author alex
 * *
 * @date 8/11/13
 */
open class AppListAsyncTask(protected val context: ApplicationContext,
                            protected val titleFilter: String,
                            private val sortOrder: String,
                            protected val listFilter: AppListFilter,
                            tag: Tag?,
                            private val completion: (List<AppInfo>, AppListFilter) -> Unit)
    : AsyncTask<Void, Void, List<AppInfo>>() {

    constructor(context: ApplicationContext, titleFilter: String, sortId: Int, listFilter: AppListFilter, tag: Tag?, completion: (List<AppInfo>, AppListFilter) -> Unit)
            : this(context, titleFilter, createSortOrder(sortId), listFilter, tag, completion)

    constructor(application: Application, titleFilter: String, sortId: Int, listFilter: AppListFilter, tag: Tag?, completion: (List<AppInfo>, AppListFilter) -> Unit)
            : this(ApplicationContext(application), titleFilter, createSortOrder(sortId), listFilter, tag, completion)

    private val contentUri = DbContentProvider.appsContentUri(tag)
    private val projection = AppListTable.projection
    private val selection: String
    private val selectionArgs: Array<String>

    init {
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

        this.selection = TextUtils.join(" AND ", selc)
        this.selectionArgs = args.toTypedArray()
    }

    override fun doInBackground(vararg params: Void?): List<AppInfo> {
        synchronized(this) {
            if (isCancelled) {
                throw OperationCanceledException()
            }
        }
        val cursor = ContentResolverCompat.query(context.contentResolver,
                contentUri, projection, selection, selectionArgs, sortOrder,
                CancellationSignal()) ?: NullCursor()
        try {
            // Ensure the cursor window is filled.
            cursor.count
        } catch (ex: RuntimeException) {
            cursor.close()
            throw ex
        }
        listFilter.resetNewCount()
        val appsCursor = AppListCursor(FilterCursor(cursor, listFilter))
        val apps = appsCursor.toList()
        appsCursor.close()
        return apps
    }

    override fun onPostExecute(result: List<AppInfo>?) {
        completion(result ?: emptyList(), listFilter)
    }

    companion object {
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
    }
}
