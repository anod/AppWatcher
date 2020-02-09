package info.anodsplace.framework.database

import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.CursorWindow
import android.database.CursorWrapper
import info.anodsplace.framework.AppLog

/**
 * @author Alex Gavrishev
 * *
 * @date 10/03/2017.
 */
abstract class CursorIterator<O>(cursor: Cursor?)
    : CursorWrapper(cursor ?: NullCursor()), CrossProcessCursor, Iterable<O>, Iterator<O> {

    class Default(cursor: Cursor?) : CursorIterator<Cursor>(cursor) {
        override val current = cursor ?: NullCursor()
    }

    init {
        this.moveToPosition(-1)
    }

    val isEmpty: Boolean
        get() = count == 0

    fun moveToNextObject(): O? {
        if (this.moveToNext()) {
            return current
        }
        return null
    }

    override fun iterator(): Iterator<O> {
        this.moveToPosition(-1)
        return this
    }

    override fun hasNext(): Boolean {
        val count = count
        val next = position + 1
        if (next >= count) {
            return false
        }

        // Make sure position isn't before the beginning of the cursor
        if (next < 0) {
            return false
        }

        // Check for no-op moves, and skip the rest of the work for them
        if (next == position) {
            return true
        }
        return position < count
    }

    abstract val current: O

    override fun next(): O {
        moveToNext()
        return current
    }

    override fun getString(columnIndex: Int): String {
        val value: String? = super.getString(columnIndex)
        if (value == null) {
            AppLog.e("$columnIndex is NULL")
            return ""
        }
        return value
    }

    fun getString(columnIndex: Int, defaultValue: String): String {
        return super.getString(columnIndex) ?: return defaultValue
    }

    override fun close() {
        if (isClosed) {
            AppLog.d("Cursor already closed")
        } else {
            super.close()
        }
    }

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
                for (i in 0 until columnNum) {
                    val field = getString(i)
                    if (!window.putString(field, getPosition(), i)) {
                        window.freeLastRow()
                        break
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
