package com.anod.appwatcher.model;

import java.util.HashMap;

import android.provider.BaseColumns;

public class AppListTable {
	
	public class Columns implements BaseColumns {
	    public static final String KEY_PACKAGE = "package";
	    public static final String KEY_VERSION_NUMBER = "ver_num";    
	    public static final String KEY_VERSION_NAME = "ver_name";
	    public static final String KEY_TITLE = "title";    
	    public static final String KEY_ICON_CACHE = "icon";
	    public static final String KEY_STATUS = "status";
	}
	public static final String TABLE_NAME = "app_list";

    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    public static final String[] APPLIST_PROJECTION = new String[] {
    	Columns._ID,
    	Columns.KEY_PACKAGE,
    	Columns.KEY_VERSION_NUMBER,
    	Columns.KEY_VERSION_NAME,
    	Columns.KEY_TITLE,
    	Columns.KEY_ICON_CACHE,
    	Columns.KEY_STATUS
    };
    
    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(Columns.KEY_PACKAGE, Columns.KEY_PACKAGE);
        map.put(Columns.KEY_VERSION_NUMBER, Columns.KEY_VERSION_NUMBER);
        map.put(Columns.KEY_VERSION_NAME, Columns.KEY_VERSION_NAME);
        map.put(Columns.KEY_TITLE, Columns.KEY_TITLE);
        map.put(Columns.KEY_ICON_CACHE, Columns.KEY_ICON_CACHE);
        map.put(Columns.KEY_STATUS, Columns.KEY_STATUS);
        return map;
    }    
}
