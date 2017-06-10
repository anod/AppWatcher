package com.anod.appwatcher.model

import android.content.Context
import android.database.Cursor
import android.net.Uri
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
open class AppListCursorLoader(context: Context, protected val mTitleFilter: String, sortOrder: String, private val mCursorFilter: FilterCursorWrapper.CursorFilter?, tag: Tag?)
    : CursorLoader(context, AppListCursorLoader.getContentUri(tag), AppListTable.PROJECTION, null, null, sortOrder) {

    private var mNewCount: Int = 0
    private var mUpdatableNewCount: Int = 0

    constructor(context: Context, titleFilter: String, sortId: Int, cursorFilter: FilterCursorWrapper.CursorFilter?, tag: Tag?)
            : this(context, titleFilter, createSortOrder(sortId), cursorFilter, tag)

    init {
        val selc = ArrayList<String>(3)
        val args = ArrayList<String>(3)

        selc.add(AppListTable.Columns.KEY_STATUS + " != ?")
        args.add(AppInfoMetadata.STATUS_DELETED.toString())

        if (tag != null) {
            selc.add(AppTagsTable.TableColumns.TAGID + " = ?")
            args.add(tag.id.toString())
            selc.add(AppTagsTable.TableColumns.APPID + " = " + AppListTable.TableColumns.APPID)
        }

        if (!TextUtils.isEmpty(mTitleFilter)) {
            selc.add(AppListTable.Columns.KEY_TITLE + " LIKE ?")
            args.add("%$mTitleFilter%")
        }

        val selection = TextUtils.join(" AND ", selc)
        val selectionArgs = args.toTypedArray()

        setSelection(selection)
        setSelectionArgs(selectionArgs)
    }

    override fun loadInBackground(): Cursor {
        val cr = super.loadInBackground()

        if (mCursorFilter == null) {
            loadNewCount()
            return AppListCursor(cr)
        } else {
            if (mCursorFilter is InstalledFilter) {
                mCursorFilter.resetNewCount()
            }
            return AppListCursor(FilterCursorWrapper(cr, mCursorFilter))
        }
    }

    val newCountFiltered: Int
        get() {
            if (mCursorFilter is InstalledFilter) {
                return mCursorFilter.newCount
            }
            return mNewCount
        }

    private fun loadNewCount() {
        val cl = DbContentProviderClient(context)
        val apps = cl.queryUpdated()

        mNewCount = 0
        mUpdatableNewCount = 0
        mNewCount = apps.count
        if (mNewCount > 0) {
            val iap = InstalledAppsProvider.PackageManager(context.packageManager)
            apps.moveToPosition(-1)
            while (apps.moveToNext()) {
                val info = apps.appInfo
                if (iap.getInfo(info.packageName).isUpdatable(info.versionNumber)) {
                    mUpdatableNewCount++
                }
            }
        }

        apps.close()
        cl.close()
    }

    val updatableCountFiltered: Int
        get() {
            if (mCursorFilter is InstalledFilter) {
                return mCursorFilter.updatableNewCount
            }
            return mUpdatableNewCount
        }

    companion object {
        private val ORDER_DEFAULT = AppListTable.Columns.KEY_STATUS + " DESC, "+ AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC"

        private fun getContentUri(tag: Tag?): Uri {
            return if (tag == null)
                DbContentProvider.APPS_CONTENT_URI
            else
                DbContentProvider.APPS_TAG_CONTENT_URI.buildUpon().appendPath(tag.id.toString()).build()
        }

        private fun createSortOrder(sortId: Int): String {
            val filter = ArrayList<String>()
            filter.add(AppListTable.Columns.KEY_STATUS + " DESC")
            if (sortId == Preferences.SORT_NAME_DESC) {
                filter.add(AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED DESC")
            } else if (sortId == Preferences.SORT_DATE_ASC) {
                filter.add(AppListTable.Columns.KEY_REFRESH_TIMESTAMP + " ASC")
            } else if (sortId == Preferences.SORT_DATE_DESC) {
                filter.add(AppListTable.Columns.KEY_REFRESH_TIMESTAMP + " DESC")
            } else {
                filter.add(AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC")
            }
            return TextUtils.join(", ", filter)
        }
    }
}
