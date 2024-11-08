package com.anod.appwatcher.database

import android.database.Cursor
import android.provider.BaseColumns
import com.anod.appwatcher.database.entities.AppChange
import info.anodsplace.framework.database.CursorIterator

/**
 * @author alex
 */
class AppChangeCursor(cursor: Cursor?) : CursorIterator<AppChange>(cursor) {

    private class Projection {
        var rowId = 0
        var appId = 1
        var versionNumber = 3
        var versionName = 4
        var details = 5
        var uploadDate = 6
        var noNewDetails = 6
    }

    private val projection = Projection()

    init {
        if (cursor != null) {
            projection.rowId = cursor.getColumnIndexOrThrow(BaseColumns._ID)
            projection.appId = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.APP_ID)
            projection.versionNumber = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.VERSION_CODE)
            projection.versionName = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.VERSION_NAME)
            projection.details = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.DETAILS)
            projection.uploadDate = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.UPLOAD_DATE)
            projection.noNewDetails = cursor.getColumnIndexOrThrow(ChangelogTable.Columns.NO_NEW_DETAILS)
        }
    }

    override val current: AppChange
        get() = AppChange(
            id = getInt(projection.rowId),
            appId = getString(projection.appId),
            versionCode = getInt(projection.versionNumber),
            versionName = getString(projection.versionName),
            details = getString(projection.details),
            uploadDate = getString(projection.uploadDate),
            noNewDetails = getInt(projection.noNewDetails) == 1
        )
}