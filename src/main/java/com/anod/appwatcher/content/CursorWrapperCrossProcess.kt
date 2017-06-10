package com.anod.appwatcher.content

import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.CursorWindow
import android.database.CursorWrapper

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

open class CursorWrapperCrossProcess(cursor: Cursor?)
    : CursorWrapper(cursor ?: NullCursor()), CrossProcessCursor {

//    @Throws(Throwable::class)
//    protected override fun finalize() {
//        // Do not remove this empty method. It is designed to prevent calls to super.
//        // Fixes bug on Droid 2, Droid Razr, where CursorWrapper finalizer closes the Cursor!
//        // @see http://stackoverflow.com/questions/6552405/android-compatibility-library-cursorloader-java-lang-illegalstateexception-cu
//    }

    /**
     * Wrapper of cursor that runs in another process should implement CrossProcessCursor
     * http://stackoverflow.com/questions/3976515/cursor-wrapping-unwrapping-in-contentprovider
     */
    override fun fillWindow(position: Int, window: CursorWindow) {
        if (position < 0 || position > count) {
            return
        }
        window.acquireReference()
        try {
            moveToPosition(position - 1)
            window.clear()
            window.startPosition = position
            val columnNum = columnCount
            window.setNumColumns(columnNum)
            while (moveToNext() && window.allocRow()) {
                for (i in 0..columnNum - 1) {
                    var putNull = true
                    val field = getString(i)
                    if (field != null) {
                        putNull = false
                        if (!window.putString(field, getPosition(), i)) {
                            window.freeLastRow()
                            break
                        }
                    }
                    //                    }
                    if (putNull) {
                        if (!window.putNull(getPosition(), i)) {
                            window.freeLastRow()
                            break
                        }
                    }
                }
            }
        } catch (e: IllegalStateException) {
            // simply ignore it
        } finally {
            window.releaseReference()
        }
    }

    override fun getWindow(): CursorWindow? {
        return null
    }

    override fun onMove(oldPosition: Int, newPosition: Int): Boolean {
        return true
    }
}
