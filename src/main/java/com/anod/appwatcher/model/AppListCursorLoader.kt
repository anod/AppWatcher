package com.anod.appwatcher.model

import android.content.Context
import android.database.Cursor
import android.support.v4.content.CursorLoader
import android.text.TextUtils
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.utils.FilterCursorWrapper
import com.anod.appwatcher.utils.InstalledAppsProvider
import java.util.*

/**
 * @author alex
 * *
 * @date 8/11/13
 */
open class AppListCursorLoader(context: Context,
                               protected val titleFilter: String,
                               sortOrder: String,
                               private val cursorFilter: FilterCursorWrapper.CursorFilter?,
                               private val tag: Tag?)
    : CursorLoader(context, DbContentProvider.appsContentUri(tag), AppListTable.projection, null, null, sortOrder) {

    private var newCount: Int = 0
    private var updatableNewCount: Int = 0

    constructor(context: Context, titleFilter: String, sortId: Int, cursorFilter: FilterCursorWrapper.CursorFilter?, tag: Tag?)
            : this(context, titleFilter, createSortOrder(sortId), cursorFilter, tag)

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
        val cr = super.loadInBackground()

        if (cursorFilter == null) {
            loadNewCount()
            return AppListCursor(cr)
        } else {
            if (cursorFilter is InstalledFilter) {
                cursorFilter.resetNewCount()
            }
            return AppListCursor(FilterCursorWrapper(cr, cursorFilter))
        }
    }

    val newCountFiltered: Int
        get() {
            if (cursorFilter is InstalledFilter) {
                return cursorFilter.newCount
            }
            return newCount
        }

    val updatableCountFiltered: Int
        get() {
            if (cursorFilter is InstalledFilter) {
                return cursorFilter.updatableNewCount
            }
            return updatableNewCount
        }

    private fun loadNewCount() {
        val cl = DbContentProviderClient(context)
        val apps = cl.queryUpdated(tag)

        newCount = 0
        updatableNewCount = 0
        newCount = apps.count
        if (newCount > 0) {
            val iap = InstalledAppsProvider.PackageManager(context.packageManager)
            apps.moveToPosition(-1)
            while (apps.moveToNext()) {
                val info = apps.appInfo
                if (iap.getInfo(info.packageName).isUpdatable(info.versionNumber)) {
                    updatableNewCount++
                }
            }
        }

        apps.close()
        cl.close()
    }

    companion object {
        private val ORDER_DEFAULT = AppListTable.Columns.status + " DESC, "+ AppListTable.Columns.title + " COLLATE LOCALIZED ASC"

        private fun createSortOrder(sortId: Int): String {
            val filter = ArrayList<String>()
            filter.add(AppListTable.Columns.status + " DESC")
            if (sortId == Preferences.SORT_NAME_DESC) {
                filter.add(AppListTable.Columns.title + " COLLATE LOCALIZED DESC")
            } else if (sortId == Preferences.SORT_DATE_ASC) {
                filter.add(AppListTable.Columns.refreshTimestamp + " ASC")
            } else if (sortId == Preferences.SORT_DATE_DESC) {
                filter.add(AppListTable.Columns.refreshTimestamp + " DESC")
            } else {
                filter.add(AppListTable.Columns.title + " COLLATE LOCALIZED ASC")
            }
            return TextUtils.join(", ", filter)
        }
    }
}
