package com.anod.appwatcher.model;

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
        public static final String KEY_STATUS = "status";
        public static final String KEY_NOTINUSE_TIMESTAMP = "update_date";
        public static final String KEY_PRICE_TEXT = "price_text";
        public static final String KEY_PRICE_CURRENCY = "price_currency";
        public static final String KEY_PRICE_MICROS = "price_micros";
        public static final String KEY_UPLOAD_DATE = "upload_date";
        public static final String KEY_DETAILS_URL = "details_url";

    }

    public static final String TABLE_NAME = "app_list";

    public static final String[] APPLIST_PROJECTION = new String[]{
            Columns._ID,
            Columns.KEY_APPID,
            Columns.KEY_PACKAGE,
            Columns.KEY_VERSION_NUMBER,
            Columns.KEY_VERSION_NAME,
            Columns.KEY_TITLE,
            Columns.KEY_CREATOR,
            Columns.KEY_ICON_CACHE,
            Columns.KEY_STATUS,
            Columns.KEY_NOTINUSE_TIMESTAMP,
            Columns.KEY_PRICE_TEXT,
            Columns.KEY_PRICE_CURRENCY,
            Columns.KEY_PRICE_MICROS,
            Columns.KEY_UPLOAD_DATE,
            Columns.KEY_DETAILS_URL
    };

}
