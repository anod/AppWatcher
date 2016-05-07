package com.anod.appwatcher.model.schema;

import android.provider.BaseColumns;

public class AppListTable {

    public class Columns implements BaseColumns {
        public static final String KEY_APPID = "app_id";
        public static final String KEY_PACKAGE = "package";
        public static final String KEY_VERSION_NUMBER = "ver_num";
        public static final String KEY_VERSION_NAME = "ver_name";
        public static final String KEY_TITLE = "title";
        public static final String KEY_CREATOR = "creator";
        public static final String KEY_ICON_CACHE = "icon";
        public static final String KEY_ICON_URL = "iconUrl";
        public static final String KEY_STATUS = "status";
        public static final String KEY_NOTINUSE_TIMESTAMP = "update_date";
        public static final String KEY_PRICE_TEXT = "price_text";
        public static final String KEY_PRICE_CURRENCY = "price_currency";
        public static final String KEY_PRICE_MICROS = "price_micros";
        public static final String KEY_UPLOAD_DATE = "upload_date";
        public static final String KEY_DETAILS_URL = "details_url";

    }

    public static final String TABLE_NAME = "app_list";

    public static final String[] PROJECTION = new String[]{
            Columns._ID,
            Columns.KEY_APPID,
            Columns.KEY_PACKAGE,
            Columns.KEY_VERSION_NUMBER,
            Columns.KEY_VERSION_NAME,
            Columns.KEY_TITLE,
            Columns.KEY_CREATOR,
            Columns.KEY_STATUS,
            Columns.KEY_NOTINUSE_TIMESTAMP,
            Columns.KEY_PRICE_TEXT,
            Columns.KEY_PRICE_CURRENCY,
            Columns.KEY_PRICE_MICROS,
            Columns.KEY_UPLOAD_DATE,
            Columns.KEY_DETAILS_URL,
            Columns.KEY_ICON_URL,
    };

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.KEY_APPID + " TEXT not null," +
                    Columns.KEY_PACKAGE + " TEXT not null," +
                    Columns.KEY_VERSION_NUMBER + " INTEGER," +
                    Columns.KEY_VERSION_NAME + " TEXT," +
                    Columns.KEY_TITLE + " TEXT not null," +
                    Columns.KEY_CREATOR + " TEXT," +
                    Columns.KEY_ICON_CACHE + " BLOB," +
                    Columns.KEY_STATUS + " INTEGER," +
                    Columns.KEY_NOTINUSE_TIMESTAMP + " INTEGER," +
                    Columns.KEY_PRICE_TEXT + " TEXT," +
                    Columns.KEY_PRICE_CURRENCY + " TEXT," +
                    Columns.KEY_PRICE_MICROS + " INTEGER," +
                    Columns.KEY_UPLOAD_DATE + " TEXT," +
                    Columns.KEY_DETAILS_URL + " TEXT," +
                    Columns.KEY_ICON_URL + " TEXT" +
                    ") ";
}
