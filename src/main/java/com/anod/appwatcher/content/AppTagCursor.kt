package com.anod.appwatcher.content

import android.database.Cursor
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.AppTagsTable
import info.anodsplace.framework.database.CursorIterator

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */
class AppTagCursor(cursor: Cursor?) : CursorIterator<AppTag>(cursor) {

    override val current: AppTag
        get() = AppTag(
                getInt(AppTagsTable.Projection._ID),
                getString(AppTagsTable.Projection.appId),
                getInt(AppTagsTable.Projection.tagId)
        )
}