package com.anod.appwatcher.model;

import java.util.HashMap;

public class AppListTable {
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_VERSION_NUMBER = "ver_num";    
    public static final String KEY_VERSION_NAME = "ver_name";
    public static final String KEY_TITLE = "title";    
    public static final String KEY_ICON_CACHE = "icon";
    public static final String KEY_STATUS = "status";
    public static final String TABLE_NAME = "app_list";

    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    public static final String[] APPLIST_PROJECTION = new String[] {
    	KEY_PACKAGE,
    	KEY_VERSION_NUMBER,
    	KEY_VERSION_NAME,
    	KEY_TITLE,
    	KEY_ICON_CACHE,
    	KEY_STATUS,
    	TABLE_NAME
    };
    
    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_PACKAGE, KEY_PACKAGE);
        map.put(KEY_VERSION_NUMBER, KEY_VERSION_NUMBER);
        map.put(KEY_VERSION_NAME, KEY_VERSION_NAME);
        map.put(KEY_TITLE, KEY_TITLE);
        map.put(KEY_ICON_CACHE, KEY_ICON_CACHE);
        map.put(KEY_STATUS, KEY_STATUS);
        return map;
    }    
}
