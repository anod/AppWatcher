package com.anod.appwatcher.model.schema;

import android.provider.BaseColumns;

/**
 * @author alex
 * @date 2015-03-01
 */
public class AppTagsTable {

    public class Columns implements BaseColumns {
        public static final String KEY_APPID = "app_id";
        public static final String KEY_TAGID = "tags_id";
    }

    public static final String TABLE_NAME = "app_tags";

    public static final String[] PROJECTION = new String[]{
            Columns._ID,
            Columns.KEY_APPID,
            Columns.KEY_TAGID
    };

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.KEY_APPID + " TEXT not null," +
                    Columns.KEY_TAGID + " INTEGER" +
                    ") ";

}
