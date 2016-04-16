package com.anod.appwatcher.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.model.schema.TagsTable;

import info.anodsplace.android.log.AppLog;

public class DbOpenHelper extends SQLiteOpenHelper {
	    
    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "app_watcher";


    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(AppListTable.TABLE_CREATE);
        db.execSQL(TagsTable.TABLE_CREATE);
        db.execSQL(AppTagsTable.TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AppLog.v("Db upgrade from: " + oldVersion + " to " + newVersion);
		switch (oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " INTEGER");
			case 2:
			case 3:
				db.execSQL("ALTER TABLE "+ AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_TEXT + " TEXT");
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_CURRENCY + " TEXT");
				db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_PRICE_MICROS + " INTEGER");
            case 4:
                db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_UPLOAD_DATE + " TEXT");
                db.execSQL("ALTER TABLE "+AppListTable.TABLE_NAME + " ADD COLUMN " + AppListTable.Columns.KEY_DETAILS_URL + " TEXT");
                db.execSQL("UPDATE "+AppListTable.TABLE_NAME + " SET " +  AppListTable.Columns.KEY_APPID + " = " + AppListTable.Columns.KEY_PACKAGE + "");
                db.execSQL("UPDATE "+AppListTable.TABLE_NAME + " SET " +  AppListTable.Columns.KEY_DETAILS_URL + " = 'details?doc=' || " + AppListTable.Columns.KEY_PACKAGE);
            case 5:
                db.execSQL(TagsTable.TABLE_CREATE);
                db.execSQL(AppTagsTable.TABLE_CREATE);
		}
	}

}
