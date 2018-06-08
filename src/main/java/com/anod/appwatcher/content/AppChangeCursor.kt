package com.anod.appwatcher.content

import android.database.Cursor
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.ChangelogTable
import info.anodsplace.framework.database.CursorIterator

/**
 * @author Alex Gavrishev
 * @date 02/09/2017
 */
class AppChangeCursor(cursor: Cursor?) : CursorIterator<AppChange>(cursor) {

    override val current: AppChange
        get() = AppChange(
                getInt(ChangelogTable.Projection._ID),
                getString(ChangelogTable.Projection.appId),
                getInt(ChangelogTable.Projection.versionCode),
                getString(ChangelogTable.Projection.versionName),
                getString(ChangelogTable.Projection.details),
                getString(ChangelogTable.Projection.uploadDate)
        )

}