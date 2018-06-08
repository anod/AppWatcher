package com.anod.appwatcher.content

import android.database.Cursor

import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.TagsTable
import info.anodsplace.framework.database.CursorIterator

/**
 * @author Alex Gavrishev
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
