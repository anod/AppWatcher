package com.anod.appwatcher.utils

import android.database.AbstractCursor
import android.database.Cursor
import android.os.Bundle

import info.anodsplace.android.log.AppLog

/**
 * Cursor wrapper that filters MIME types not matching given list.
 */
class FilterCursorWrapper(private val mCursor: Cursor, filter: FilterCursorWrapper.CursorFilter?) : AbstractCursor() {
    private var mPosition: IntArray? = null
    private var mCount: Int = mCursor.count
    private val mFilterEnabled = (filter != null)

    interface CursorFilter {
        fun filterRecord(cursor: Cursor): Boolean
    }

    init {
        if (filter != null) {
            initPositions(mCursor, count, filter)
        }

        AppLog.d("Before filtering " + mCursor.count + ", after " + mCount)
    }

    private fun initPositions(cursor: Cursor, count: Int, filter: CursorFilter) {
        mCount = 0
        mPosition = IntArray(mCursor.count)
        cursor.moveToPosition(-1)
        while (cursor.moveToNext() && mCount < count) {
            if (!filter.filterRecord(cursor)) {
                mPosition!![mCount++] = cursor.position
            }
        }
    }

    override fun getExtras(): Bundle {
        return mCursor.extras
    }

    override fun close() {
        super.close()
        mCursor.close()
    }

    override fun onMove(oldPosition: Int, newPosition: Int): Boolean {
        return if (mFilterEnabled) mCursor.moveToPosition(mPosition!![newPosition]) else true
    }

    override fun getColumnNames(): Array<String> {
        return mCursor.columnNames
    }

    override fun getCount(): Int {
        return mCount
    }

    override fun getDouble(column: Int): Double {
        return mCursor.getDouble(column)
    }

    override fun getFloat(column: Int): Float {
        return mCursor.getFloat(column)
    }

    override fun getInt(column: Int): Int {
        return mCursor.getInt(column)
    }

    override fun getLong(column: Int): Long {
        return mCursor.getLong(column)
    }

    override fun getShort(column: Int): Short {
        return mCursor.getShort(column)
    }

    override fun getString(column: Int): String {
        return mCursor.getString(column)
    }

    override fun getType(column: Int): Int {
        return mCursor.getType(column)
    }

    override fun isNull(column: Int): Boolean {
        return mCursor.isNull(column)
    }

    override fun getBlob(column: Int): ByteArray {
        return mCursor.getBlob(column)
    }
}
