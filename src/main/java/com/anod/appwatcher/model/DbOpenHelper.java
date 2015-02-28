package com.anod.appwatcher.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anod.appwatcher.utils.AppLog;

public class DbOpenHelper extends SQLiteOpenHelper {
	    
    private static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "app_watcher";
    private static final String TABLE_CREATE =
    	"CREATE TABLE " + AppListTable.TABLE_NAME + " (" +
    		AppListTable.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			AppListTable.Columns.KEY_APPID + " TEXT not null," +
			AppListTable.Columns.KEY_PACKAGE + " TEXT not null," +
			AppListTable.Columns.KEY_VERSION_NUMBER + " INTEGER," +
			AppListTable.Columns.KEY_VERSION_NAME + " TEXT," + 
			AppListTable.Columns.KEY_TITLE + " TEXT not null," +
			AppListTable.Columns.KEY_CREATOR + " TEXT," + 
			AppListTable.Columns.KEY_ICON_CACHE + " BLOB," +
			AppListTable.Columns.KEY_STATUS + " INTEGER," +
            AppListTable.Columns.KEY_NOTINUSE_TIMESTAMP + " INTEGER," +
			AppListTable.Columns.KEY_PRICE_TEXT + " TEXT," +
			AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT," +
			AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER," +
            AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT," +
            AppListTable.Columns.KEY_DETAILS_URL + " TEXT" +
    ") ";

    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    
	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL(TABLE_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AppLog.v("Db upgrade from: "+oldVersion+" to "+newVersion);
		switch (oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " INTEGER");
			case 2:
			case 3:
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_TEXT + " TEXT");
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT");
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER");
            case 4:
                db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT");
                db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT");
                db.execSQL("UPDATE "+AppListTable.TABLE_NAME + " SET " +  AppListTable.Columns.KEY_APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "");
                db.execSQL("UPDATE "+AppListTable.TABLE_NAME + " SET " +  AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE);
		}
	}

}
