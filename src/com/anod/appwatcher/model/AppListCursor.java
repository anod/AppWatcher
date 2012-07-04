package com.anod.appwatcher.model;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AppListCursor extends CursorWrapper {
    private static final int IDX_ROWID = 0;	
    private static final int IDX_APPID = 1;	
    private static final int IDX_PACKAGE = 2;
    private static final int IDX_VERSION_NUMBER = 3;    
    private static final int IDX_VERSION_NAME = 4;
    private static final int IDX_TITLE = 5;    
    private static final int IDX_CREATOR = 6;    
    private static final int IDX_ICON_CACHE = 7;
    private static final int IDX_STATUS = 8;
    private static final int IDX_UPDATE_DATE = 9;

	public AppListCursor(Cursor cursor) {
		super(cursor);
	}

	public AppInfo getAppInfo() {
		Bitmap icon = null;
		byte[] iconData = getBlob(IDX_ICON_CACHE);
		if (iconData != null && iconData.length > 0) {
			icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
		}
		return new AppInfo(
			getInt(IDX_ROWID),
			getString(IDX_APPID),
			getString(IDX_PACKAGE),
			getInt(IDX_VERSION_NUMBER),
			getString(IDX_VERSION_NAME),
			getString(IDX_TITLE),
			getString(IDX_CREATOR),			
			icon,
			getInt(IDX_STATUS),
			getInt(IDX_UPDATE_DATE)
		);
	}
	
	protected void finalize() throws Throwable {
	    // Do not remove this empty method. It is designed to prevent calls to super.
	    // Fixes bug on Droid 2, Droid Razr, where CursorWrapper finalizer closes the Cursor!
		// @see http://stackoverflow.com/questions/6552405/android-compatibility-library-cursorloader-java-lang-illegalstateexception-cu
	}	
}
