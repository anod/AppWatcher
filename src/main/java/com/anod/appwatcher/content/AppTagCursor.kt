package com.anod.appwatcher.content

import android.database.Cursor
import com.anod.appwatcher.model.AppTag
import com.anod.appwatcher.model.schema.AppTagsTable
import info.anodsplace.appwatcher.framework.CursorIterator

/**
 * @author algavris
 * @date 26/06/2017
 */
class AppTagCursor(cursor: Cursor?) : CursorIterator<AppTag>(cursor) {

    override fun next(): AppTag {
        return AppTag(
            getString(AppTagsTable.Projection.appId),
            getInt(AppTagsTable.Projection.tagId)
        )
    }
}