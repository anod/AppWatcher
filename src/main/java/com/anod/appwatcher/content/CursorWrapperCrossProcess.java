package com.anod.appwatcher.content;

import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;

/**
 * @author algavris
 * @date 10/03/2017.
 */

class CursorWrapperCrossProcess extends CursorWrapper implements CrossProcessCursor {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    CursorWrapperCrossProcess(Cursor cursor) {
        super(cursor == null ? new NullCursor() : cursor);
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
