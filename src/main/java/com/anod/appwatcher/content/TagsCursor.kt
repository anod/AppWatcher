package com.anod.appwatcher.content

import android.database.Cursor

import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.TagsTable

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class TagsCursor(cursor: Cursor?) : CursorIterator<Tag>(cursor) {

    val tag: Tag
        get() = Tag(
            getInt(TagsTable.Projection._ID),
            getString(TagsTable.Projection.NAME),
            getInt(TagsTable.Projection.COLOR)
        )

    override fun next(): Tag {
        return this.tag
    }
}
