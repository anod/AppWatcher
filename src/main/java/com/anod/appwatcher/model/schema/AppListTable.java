package com.anod.appwatcher.model.schema;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.anod.appwatcher.model.AppInfo;

public class AppListTable {

    public static class Columns implements BaseColumns {
        public static final String APPID = "app_id";
        public static final String KEY_PACKAGE = "package";
        public static final String KEY_VERSION_NUMBER = "ver_num";
        public static final String KEY_VERSION_NAME = "ver_name";
        public static final String KEY_TITLE = "title";
        public static final String KEY_CREATOR = "creator";
        public static final String KEY_ICON_CACHE = "icon";
        public static final String KEY_ICON_URL = "iconUrl";
        public static final String KEY_STATUS = "status";
        public static final String KEY_REFRESH_TIMESTAMP = "update_date";
        public static final String KEY_PRICE_TEXT = "price_text";
        public static final String KEY_PRICE_CURRENCY = "price_currency";
        public static final String KEY_PRICE_MICROS = "price_micros";
        public static final String KEY_UPLOAD_DATE = "upload_date";
        public static final String KEY_DETAILS_URL = "details_url";
        public static final String KEY_APP_TYPE = "app_type";
        public static final String KEY_SYNC_VERSION = "sync_version";
    }

    public class TableColumns {
        public static final String _ID = AppListTable.TABLE_NAME + "." + AppListTable.Columns._ID;
        public static final String APPID = AppListTable.TABLE_NAME + ".app_id";
    }

    public static final String TABLE_NAME = "app_list";

    public static final String[] PROJECTION = new String[]{
            TableColumns._ID,
            TableColumns.APPID,
            Columns.KEY_PACKAGE,
            Columns.KEY_VERSION_NUMBER,
            Columns.KEY_VERSION_NAME,
            Columns.KEY_TITLE,
            Columns.KEY_CREATOR,
            Columns.KEY_STATUS,
            Columns.KEY_REFRESH_TIMESTAMP,
            Columns.KEY_PRICE_TEXT,
            Columns.KEY_PRICE_CURRENCY,
            Columns.KEY_PRICE_MICROS,
            Columns.KEY_UPLOAD_DATE,
            Columns.KEY_DETAILS_URL,
            Columns.KEY_ICON_URL,
            Columns.KEY_APP_TYPE,
            Columns.KEY_SYNC_VERSION
    };

    public static class Projection {
        public static final int _ID = 0;
        public static final int APPID = 1;
        public static final int PACKAGE = 2;
        public static final int VERSION_NUMBER = 3;
        public static final int VERSION_NAME = 4;
        public static final int TITLE = 5;
        public static final int CREATOR = 6;
        public static final int STATUS = 7;
        public static final int REFRESH_TIME= 8;
        public static final int PRICE_TEXT = 9;
        public static final int PRICE_CURRENCY = 10;
        public static final int PRICE_MICROS = 11;
        public static final int UPLOAD_DATE = 12;
        public static final int DETAILS_URL = 13;
        public static final int ICON_URL = 14;
        public static final int APP_TYPE = 15;
        public static final int SYNC_VERSION = 16;
    }

    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Columns.APPID + " TEXT not null," +
                    Columns.KEY_PACKAGE + " TEXT not null," +
                    Columns.KEY_VERSION_NUMBER + " INTEGER," +
                    Columns.KEY_VERSION_NAME + " TEXT," +
                    Columns.KEY_TITLE + " TEXT not null," +
                    Columns.KEY_CREATOR + " TEXT," +
                    Columns.KEY_ICON_CACHE + " BLOB," +
                    Columns.KEY_STATUS + " INTEGER," +
                    Columns.KEY_REFRESH_TIMESTAMP + " INTEGER," +
                    Columns.KEY_PRICE_TEXT + " TEXT," +
                    Columns.KEY_PRICE_CURRENCY + " TEXT," +
                    Columns.KEY_PRICE_MICROS + " INTEGER," +
                    Columns.KEY_UPLOAD_DATE + " TEXT," +
                    Columns.KEY_DETAILS_URL + " TEXT," +
                    Columns.KEY_ICON_URL + " TEXT," +
                    Columns.KEY_APP_TYPE + " TEXT," +
                    Columns.KEY_SYNC_VERSION + " INTEGER" +
                    ") ";

    /**
     * @return Content values for app
     */
    public static ContentValues createContentValues(AppInfo app) {
        ContentValues values = new ContentValues();

        values.put(AppListTable.Columns.APPID, app.getAppId());
        values.put(AppListTable.Columns.KEY_PACKAGE, app.packageName);
        values.put(AppListTable.Columns.KEY_TITLE, app.title);
        values.put(AppListTable.Columns.KEY_VERSION_NUMBER, app.versionNumber);
        values.put(AppListTable.Columns.KEY_VERSION_NAME, app.versionName);
        values.put(AppListTable.Columns.KEY_CREATOR, app.creator);
        values.put(AppListTable.Columns.KEY_STATUS, app.getStatus());
        values.put(AppListTable.Columns.KEY_UPLOAD_DATE, app.uploadDate);

        values.put(AppListTable.Columns.KEY_PRICE_TEXT, app.priceText);
        values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, app.priceCur);
        values.put(AppListTable.Columns.KEY_PRICE_MICROS, app.priceMicros);

        values.put(AppListTable.Columns.KEY_DETAILS_URL, app.getDetailsUrl());

        values.put(AppListTable.Columns.KEY_ICON_URL, app.iconUrl);
        values.put(AppListTable.Columns.KEY_REFRESH_TIMESTAMP, app.refreshTime);

        values.put(AppListTable.Columns.KEY_APP_TYPE, app.appType);
        values.put(AppListTable.Columns.KEY_SYNC_VERSION, app.syncVersion);
        return values;
    }
}
