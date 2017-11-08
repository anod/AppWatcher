package com.anod.appwatcher.framework

import android.database.AbstractCursor
import android.database.Cursor
import android.os.Bundle

import info.anodsplace.android.log.AppLog

/**
 * Cursor wrapper that filters MIME types not matching given list.
 */
class FilterCursor(private val cursor: Cursor, filter: CursorFilter?) : AbstractCursor() {
    private var position: IntArray? = null
    private var count: Int = cursor.count
    private val filterEnabled = (filter != null)

    interface CursorFilter {
        fun filterRecord(cursor: Cursor): Boolean
    }

    init {
        if (filter != null) {
            initPositions(cursor, count, filter)
        }

        AppLog.d("Before filtering " + cursor.count + ", after " + count)
    }

    private fun initPositions(cursor: Cursor, count: Int, filter: CursorFilter) {
        this.count = 0
        position = IntArray(this.cursor.count)
        cursor.moveToPosition(-1)
        while (cursor.moveToNext() && count < count) {
            if (!filter.filterRecord(cursor)) {
                position!![this.count++] = cursor.position
            }
        }
    }

    override fun getExtras(): Bundle {
        return cursor.extras
    }

    override fun close() {
        super.close()
        cursor.close()
    }

    override fun onMove(oldPosition: Int, newPosition: Int): Boolean {
        return if (filterEnabled) cursor.moveToPosition(position!![newPosition]) else true
    }

    override fun getColumnNames(): Array<String> {
        return cursor.columnNames
    }

    override fun getCount(): Int {
        return count
    }

    override fun getDouble(column: Int): Double {
        return cursor.getDouble(column)
    }

    override fun getFloat(column: Int): Float {
        return cursor.getFloat(column)
    }

    override fun getInt(column: Int): Int {
        return cursor.getInt(column)
    }

    override fun getLong(column: Int): Long {
        return cursor.getLong(column)
    }

    override fun getShort(column: Int): Short {
        return cursor.getShort(column)
    }

    override fun getString(column: Int): String {
        return cursor.getString(column)
    }

    override fun getType(column: Int): Int {
        return cursor.getType(column)
    }

    override fun isNull(column: Int): Boolean {
        return cursor.isNull(column)
    }

    override fun getBlob(column: Int): ByteArray {
        return cursor.getBlob(column)
    }
}
