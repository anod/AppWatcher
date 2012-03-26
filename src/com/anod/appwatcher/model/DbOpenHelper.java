package com.anod.appwatcher.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {
	    
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "app_watcher";
    private static final String TABLE_CREATE =
    	"CREATE TABLE " + AppListTable.TABLE_NAME + " (" +
			AppListTable.KEY_PACKAGE + " TEXT not null," +
			AppListTable.KEY_VERSION_NUMBER + " INTEGER," +
			AppListTable.KEY_VERSION_NAME + " INTEGER," +    		
			AppListTable.KEY_TITLE + " TEXT," +
			AppListTable.KEY_ICON_CACHE + " BLOB," +
			AppListTable.KEY_STATUS + " INTEGER" +    		
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
		// TODO Auto-generated method stub
		
	}

}
