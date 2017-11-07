package com.anod.appwatcher.content

import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.CursorWindow
import android.database.CursorWrapper
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */
abstract class CursorIterator<O>(cursor: Cursor?)
    : CursorWrapper(cursor ?: NullCursor()), CrossProcessCursor, Iterable<O>, Iterator<O> {

    class Default(cursor: Cursor?): CursorIterator<Cursor>(cursor) {
        override fun next(): Cursor {
            return this
        }
    }

    init {
        this.moveToPosition(-1)
    }

    fun moveToNextObject(): O? {
        if (this.moveToNext()) {
            return this.next()
        }
        return null
    }

    override fun iterator(): Iterator<O> {
        return this
    }

    override fun hasNext(): Boolean {
        return moveToNext()
    }

    override fun getString(columnIndex: Int): String {
        val value: String? = super.getString(columnIndex)
        if (value == null) {
            AppLog.e("$columnIndex is NULL")
            return ""
        }
        return value
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
