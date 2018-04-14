package com.anod.appwatcher.model

import android.content.Context
import android.database.Cursor
import android.support.v4.content.CursorLoader
import android.text.TextUtils
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.database.FilterCursor
import info.anodsplace.framework.database.NullCursor
import java.util.*

/**
 * @author alex
 * *
 * @date 8/11/13
 */
open class AppListCursorLoader(context: Context,
                               protected val titleFilter: String,
                               sortOrder: String,
                               private val cursorFilter: AppListFilter,
                               tag: Tag?)
    : CursorLoader(context, DbContentProvider.appsContentUri(tag), AppListTable.projection, null, null, sortOrder) {

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

        val selection = TextUtils.join(" AND ", selc)
        val selectionArgs = args.toTypedArray()

        setSelection(selection)
        setSelectionArgs(selectionArgs)
    }

    override fun loadInBackground(): Cursor {
        val cr = super.loadInBackground() ?: NullCursor()

        cursorFilter.resetNewCount()
        return AppListCursor(FilterCursor(cr, cursorFilter))
    }
}
