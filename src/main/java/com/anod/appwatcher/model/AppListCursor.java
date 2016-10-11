package com.anod.appwatcher.model;

import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;

/**
 * @author alex
 */
public class AppListCursor extends CursorWrapper implements CrossProcessCursor {
    private static final int IDX_ROWID = 0;
    private static final int IDX_APPID = 1;
    static final int IDX_PACKAGE = 2;
    static final int IDX_VERSION_NUMBER = 3;
    private static final int IDX_VERSION_NAME = 4;
    private static final int IDX_TITLE = 5;
    private static final int IDX_CREATOR = 6;
    static final int IDX_STATUS = 7;
    static final int IDX_REFRESH_TIME= 8;
    private static final int IDX_PRICE_TEXT = 9;
    private static final int IDX_PRICE_CURRENCY = 10;
    private static final int IDX_PRICE_MICROS = 11;
    private static final int IDX_UPLOAD_DATE = 12;
    private static final int IDX_DETAILS_URL = 13;
    private static final int IDX_ICON_URL = 14;

    public AppListCursor(Cursor cursor) {
        super(cursor);
    }


    public String getAppId() {
        return getString(IDX_APPID);
    }

    public AppInfo getAppInfo() {

        return new AppInfo(
                getInt(IDX_ROWID),
                getString(IDX_APPID),
                getString(IDX_PACKAGE),
                getInt(IDX_VERSION_NUMBER),
                getString(IDX_VERSION_NAME),
                getString(IDX_TITLE),
                getString(IDX_CREATOR),
                getString(IDX_ICON_URL),
                getInt(IDX_STATUS),
                getString(IDX_UPLOAD_DATE),
                getString(IDX_PRICE_TEXT),
                getString(IDX_PRICE_CURRENCY),
                getInt(IDX_PRICE_MICROS),
                getString(IDX_DETAILS_URL),
                getLong(IDX_REFRESH_TIME)
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
                    boolean putNull = true;
                    ;
//                    if (IDX_ICON_CACHE == i) {
//                        byte[] iconData = getBlob(IDX_ICON_CACHE);
//                        if (iconData != null && iconData.length > 0) {
//                            putNull = false;
//                            if (!window.putBlob(iconData, getPosition(), i)) {
//                                window.freeLastRow();
//                                break;
//                            }
//                        }
//                    } else {
                        String field = getString(i);
                        if (field != null) {
                            putNull = false;
                            if (!window.putString(field, getPosition(), i)) {
                                window.freeLastRow();
                                break;
                            }
                        }
//                    }
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
