package com.anod.appwatcher.watchlist

import android.app.Application
import android.database.Cursor
import android.text.TextUtils
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.CursorAsyncTask
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.database.FilterCursor
import java.util.*

/**
 * @author alex
 * *
 * @date 8/11/13
 */
open class AppListAsyncTask(context: ApplicationContext,
                            selection: Pair<String,Array<String>>,
                            sortOrder: String,
                            protected val listFilter: AppListFilter,
                            tag: Tag?,
                            private val completion: (List<AppInfo>, AppListFilter) -> Unit)
    : CursorAsyncTask<AppInfo, AppListCursor>(
        context,
        DbContentProvider.appsContentUri(tag),
        AppListTable.projection,
        selection.first,
        selection.second,
        sortOrder) {

    constructor(context: ApplicationContext, titleFilter: String, sortId: Int, listFilter: AppListFilter, tag: Tag?, completion: (List<AppInfo>, AppListFilter) -> Unit)
            : this(context, createSelection(tag, titleFilter), createSortOrder(sortId), listFilter, tag, completion)

    constructor(application: Application, titleFilter: String, sortId: Int, listFilter: AppListFilter, tag: Tag?, completion: (List<AppInfo>, AppListFilter) -> Unit)
            : this(ApplicationContext(application), createSelection(tag, titleFilter), createSortOrder(sortId), listFilter, tag, completion)

    override fun convert(cursor: Cursor): AppListCursor {
        return AppListCursor(FilterCursor(cursor, listFilter))
    }

    override fun doInBackground(vararg params: Void?): List<AppInfo> {
        listFilter.resetNewCount() // TODO: Remove, no needed anymore
        return super.doInBackground(*params)
    }

    override fun onPostExecute(result: List<AppInfo>?) {
        completion(result ?: emptyList(), listFilter)
    }

    companion object {
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
