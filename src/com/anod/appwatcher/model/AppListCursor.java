package com.anod.appwatcher.model;

import android.database.Cursor;
import android.database.CursorWrapper;

public class AppListCursor extends CursorWrapper {

	public AppListCursor(Cursor cursor) {
		super(cursor);
	}

	public AppInfo getAppInfo() {
		return null;
	}
}
