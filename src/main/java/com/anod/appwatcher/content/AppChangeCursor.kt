package com.anod.appwatcher.content

import android.database.Cursor
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.content.schema.ChangelogTable
import info.anodsplace.framework.database.CursorIterator

/**
 * @author algavris
 * @date 02/09/2017
 */
class AppChangeCursor(cursor: Cursor?) : CursorIterator<AppChange>(cursor) {

    private val change: AppChange
        get() = AppChange(
                getString(ChangelogTable.Projection.appId),
                getInt(ChangelogTable.Projection.versionCode),
                getString(ChangelogTable.Projection.versionName),
                getString(ChangelogTable.Projection.details),
                getString(ChangelogTable.Projection.uploadDate)
        )

    override fun next(): AppChange {
        return this.change
    }
}