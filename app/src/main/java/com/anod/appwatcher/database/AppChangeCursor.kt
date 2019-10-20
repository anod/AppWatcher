package com.anod.appwatcher.database

import android.database.Cursor
import android.provider.BaseColumns
import androidx.room.util.CursorUtil
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
            projection.rowId = CursorUtil.getColumnIndexOrThrow(cursor, BaseColumns._ID)
            projection.appId = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.appId)
            projection.versionNumber = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.versionCode)
            projection.versionName = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.versionName)
            projection.details = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.details)
            projection.uploadDate = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.uploadDate)
            projection.noNewDetails = CursorUtil.getColumnIndexOrThrow(cursor, ChangelogTable.Columns.noNewDetails)
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
