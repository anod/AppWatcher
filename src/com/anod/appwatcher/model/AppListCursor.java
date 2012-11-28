package com.anod.appwatcher.model;

import com.anod.appwatcher.utils.BitmapUtils;

import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
/**
 * 
 * @author alex
 *
 */
public class AppListCursor extends CursorWrapper implements CrossProcessCursor {
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
		byte[] iconData = getBlob(IDX_ICON_CACHE);
		Bitmap icon = BitmapUtils.unFlattenBitmap(iconData);

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
			getLong(IDX_UPDATE_DATE)
		);
	}
	
	protected void finalize() throws Throwable {
	    // Do not remove this empty method. It is designed to prevent calls to super.
	    // Fixes bug on Droid 2, Droid Razr, where CursorWrapper finalizer closes the Cursor!
		// @see http://stackoverflow.com/questions/6552405/android-compatibility-library-cursorloader-java-lang-illegalstateexception-cu
	}

	/**
	 * Wrapper of cursor that runs in another process should implement CrossProcessCursor
	 * http://stackoverflow.com/questions/3976515/cursor-wrapping-unwrapping-in-contentprovider
	 */
	@Override
	public void fillWindow(int position, CursorWindow window) {
		if (position < 0 || position > getCount()) {
			return;
		}
		window.acquireReference();
		try {
			moveToPosition(position - 1);
			window.clear();
			window.setStartPosition(position);
			int columnNum = getColumnCount();
			window.setNumColumns(columnNum);
			while (moveToNext() && window.allocRow()) {
				for (int i = 0; i < columnNum; i++) {
					boolean putNull = true;;
					if (IDX_ICON_CACHE == i) {
						byte[] iconData = getBlob(IDX_ICON_CACHE);
						if (iconData!= null && iconData.length > 0) {
							putNull = false;
							if (!window.putBlob(iconData, getPosition(), i)) {
								window.freeLastRow();
								break;
							}
						}
					} else {
						String field = getString(i);
						if (field != null) {
							putNull = false;
							if (!window.putString(field, getPosition(), i)) {
								window.freeLastRow();
								break;
							}
						}
					}
					if (putNull) {
						if (!window.putNull(getPosition(), i)) {
							window.freeLastRow();
							break;
						}
					}
				}
			}
		} catch (IllegalStateException e) {
			// simply ignore it
		} finally {
			window.releaseReference();
		}
	}

	@Override
	public CursorWindow getWindow() {
		return null;
	}

	@Override
	public boolean onMove(int oldPosition, int newPosition) {
		return true;
	}	
}
