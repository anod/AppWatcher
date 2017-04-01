package com.anod.appwatcher.model.schema;

import android.provider.BaseColumns;

/**
 * @author alex
 * @date 2015-03-01
 */
public class AppTagsTable {

    private class Columns implements BaseColumns {
        public static final String APPID = "app_id";
        public static final String TAGID = "tags_id";
    }

    public class TableColumns {
        public static final String _ID = AppTagsTable.TABLE_NAME + "." + Columns._ID;
        public static final String APPID = AppTagsTable.TABLE_NAME + ".app_id";
        public static final String TAGID = AppTagsTable.TABLE_NAME + ".tags_id";
    }

    public static final String TABLE_NAME = "app_tags";

    public static final String[] PROJECTION = new String[]{
            TableColumns._ID,
            TableColumns.APPID,
            TableColumns.TAGID
    };

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.APPID + " TEXT not null," +
                    Columns.TAGID + " INTEGER" +
                    ") ";

}
