package com.anod.appwatcher.content

import android.database.Cursor

import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.content.schema.TagsTable
import info.anodsplace.framework.database.CursorIterator

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class TagsCursor(cursor: Cursor?) : CursorIterator<Tag>(cursor) {

    override val current: Tag
        get() = Tag(
            getInt(TagsTable.Projection._ID),
            getString(TagsTable.Projection.name),
            getInt(TagsTable.Projection.color)
        )
}
