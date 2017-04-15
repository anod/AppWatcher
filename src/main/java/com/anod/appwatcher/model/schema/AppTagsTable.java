package com.anod.appwatcher.model.schema;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.anod.appwatcher.model.Tag;

/**
 * @author alex
 * @date 2015-03-01
 */
public class AppTagsTable {

    public class Columns implements BaseColumns {
        public static final String APPID = "app_id";
        public static final String TAGID = "tags_id";
    }

    public class TableColumns {
        public static final String _ID = AppTagsTable.TABLE_NAME + "." + Columns._ID;
        public static final String APPID = AppTagsTable.TABLE_NAME + ".app_id";
        public static final String TAGID = AppTagsTable.TABLE_NAME + ".tags_id";
    }

    public static final String TABLE_NAME = "app_tags";

    public static class Projection {
        public static final int _ID = 0;
        public static final int APPID = 1;
        public static final int TAGID = 2;
    }

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

    public static ContentValues createContentValues(String appId, Tag tag) {
        ContentValues values = new ContentValues();
        values.put(Columns.APPID, appId);
        values.put(Columns.TAGID, tag.id);
        return values;
    }
}
