package com.anod.appwatcher.content

import android.database.Cursor

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.schema.AppListTable

/**
 * @author alex
 */
class AppListCursor(cursor: Cursor?) : CursorWrapperCrossProcess(cursor) {

    val appInfo: AppInfo
        get() = AppInfo(
                getInt(AppListTable.Projection._ID),
                getString(AppListTable.Projection.APPID),
                getString(AppListTable.Projection.PACKAGE),
                getInt(AppListTable.Projection.VERSION_NUMBER),
                getString(AppListTable.Projection.VERSION_NAME),
                getString(AppListTable.Projection.TITLE),
                getString(AppListTable.Projection.CREATOR),
                getString(AppListTable.Projection.ICON_URL),
                getInt(AppListTable.Projection.STATUS),
                getString(AppListTable.Projection.UPLOAD_DATE),
                getString(AppListTable.Projection.PRICE_TEXT),
                getString(AppListTable.Projection.PRICE_CURRENCY),
                getInt(AppListTable.Projection.PRICE_MICROS),
                getString(AppListTable.Projection.DETAILS_URL),
                getLong(AppListTable.Projection.REFRESH_TIME),
                getString(AppListTable.Projection.APP_TYPE),
                getInt(AppListTable.Projection.SYNC_VERSION)
        )

}
