package com.anod.appwatcher.model.schema;

import android.provider.BaseColumns;

/**
 * @author alex
 * @date 2015-03-01
 */
public class TagsTable {

    public class Columns implements BaseColumns {
        public static final String KEY_NAME = "name";
        public static final String KEY_COLOR = "color";
    }

    public static final String TABLE_NAME = "tags";

    public static final String[] PROJECTION = new String[]{
            Columns._ID,
            Columns.KEY_NAME,
            Columns.KEY_COLOR
    };

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.KEY_NAME + " TEXT not null," +
                    Columns.KEY_COLOR + " INTEGER" +
                    ") ";
}
