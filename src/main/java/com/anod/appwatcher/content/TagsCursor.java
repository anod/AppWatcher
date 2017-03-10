package com.anod.appwatcher.content;

import android.database.Cursor;

import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.TagsTable;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class TagsCursor extends CursorWrapperCrossProcess {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public TagsCursor(Cursor cursor) {
        super(cursor);
    }

    public Tag getTag()
    {
        return new Tag(
                getInt(TagsTable.Projection._ID),
                getString(TagsTable.Projection.NAME),
                getInt(TagsTable.Projection.COLOR)
        );
    }
}
