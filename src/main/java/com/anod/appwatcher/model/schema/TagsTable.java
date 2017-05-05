package com.anod.appwatcher.model.schema;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.anod.appwatcher.model.Tag;

/**
 * @author alex
 * @date 2015-03-01
 */
public class TagsTable {

    public class Columns implements BaseColumns {
        public static final String NAME = "name";
        public static final String COLOR = "color";
    }

    public static final String TABLE_NAME = "tags";

    public class TableColumns {
        public static final String _ID = TagsTable.TABLE_NAME + "." + TagsTable.Columns._ID;
        public static final String NAME = TagsTable.TABLE_NAME + ".name";
        public static final String COLOR = TagsTable.TABLE_NAME + ".color";
    }

    public static final String[] PROJECTION = new String[] {
            TableColumns._ID,
            TableColumns.NAME,
            TableColumns.COLOR
    };

    public static class Projection {
        public static final int _ID = 0;
        public static final int NAME = 1;
        public static final int COLOR = 2;
    }

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.NAME + " TEXT not null," +
                    Columns.COLOR + " INTEGER" +
                    ") ";

    public static ContentValues createContentValues(Tag tag) {
        ContentValues values = new ContentValues();
        if (tag.id > 0) {
            values.put(Columns._ID, tag.id);
        }
        values.put(Columns.NAME, tag.name);
        values.put(Columns.COLOR, tag.color);
        return values;
    }

}
