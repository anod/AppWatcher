package com.anod.appwatcher.utils;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.Bundle;

import info.anodsplace.android.log.AppLog;

/**
 * Cursor wrapper that filters MIME types not matching given list.
 */
public class FilterCursorWrapper extends AbstractCursor {
    private final Cursor mCursor;
    private final int[] mPosition;
    private int mCount;
    private final boolean mFilterEnabled;

    public interface CursorFilter {
         boolean filterRecord(Cursor cursor);
    }

    public FilterCursorWrapper(Cursor cursor, CursorFilter filter) {
        mCursor = cursor;
        final int count = cursor.getCount();

        if (filter == null) {
            mFilterEnabled = false;
            mCount = count;
            mPosition = null;
        } else {
            mFilterEnabled = true;
            mPosition = new int[count];
        }

        if (mFilterEnabled) {
            initPositions(cursor, count, filter);
        }
        AppLog.d("Before filtering " + cursor.getCount() + ", after " + mCount);
    }

    private void initPositions(Cursor cursor, int count, CursorFilter filter) {
        cursor.moveToPosition(-1);
        while (cursor.moveToNext() && mCount < count) {
            if (!filter.filterRecord(cursor)) {
                mPosition[mCount++] = cursor.getPosition();
            }
        }
    }

    @Override
    public Bundle getExtras() {
        return mCursor.getExtras();
    }
    @Override
    public void close() {
        super.close();
        mCursor.close();
    }
    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return (mFilterEnabled) ? mCursor.moveToPosition(mPosition[newPosition]) : true;
    }
    @Override
    public String[] getColumnNames() {
        return mCursor.getColumnNames();
    }
    @Override
    public int getCount() {
        return mCount;
    }
    @Override
    public double getDouble(int column) {
        return mCursor.getDouble(column);
    }
    @Override
    public float getFloat(int column) {
        return mCursor.getFloat(column);
    }
    @Override
    public int getInt(int column) {
        return mCursor.getInt(column);
    }
    @Override
    public long getLong(int column) {
        return mCursor.getLong(column);
    }
    @Override
    public short getShort(int column) {
        return mCursor.getShort(column);
    }
    @Override
    public String getString(int column) {
        return mCursor.getString(column);
    }
    @Override
    public int getType(int column) {
        return mCursor.getType(column);
    }
    @Override
    public boolean isNull(int column) {
        return mCursor.isNull(column);
    }
    @Override
    public byte[] getBlob(int column) {
        return mCursor.getBlob(column);
    }
}
