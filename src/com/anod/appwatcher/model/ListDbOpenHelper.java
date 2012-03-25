package com.anod.appwatcher.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ListDbOpenHelper extends SQLiteOpenHelper {
	
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_VERSION_NUMBER = "ver_num";    
    public static final String KEY_VERSION_NAME = "ver_name";
    public static final String KEY_ICON_CACHE = "ver_name";
    
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "app_watcher";    
    private static final String TABLE_NAME = "app_list";
    private static final String TABLE_CREATE =
    		"CREATE TABLE " + TABLE_NAME + " (" +
                KEY_PACKAGE + " TEXT) ";
    
    ListDbOpenHelper(Context context) {
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
