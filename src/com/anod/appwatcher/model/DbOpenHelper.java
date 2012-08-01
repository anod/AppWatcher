package com.anod.appwatcher.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {
	    
    private static final int DATABASE_VERSION = 3;
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
			AppListTable.Columns.KEY_UPDATE_DATE + " INTEGER" +
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
		// Version 2
		db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPDATE_DATE + " INTEGER");
	}

}
