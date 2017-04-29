package com.anod.appwatcher.backup;

import com.android.util.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import info.anodsplace.android.log.AppLog;

/**
 * @author algavris
 * @date 29/04/2017.
 */

public class JsonArrayIterator<T> implements Iterator<T> {
    private final JsonReader mJsonReader;
    private final ObjectJsonReader<T> mObjectReader;
    private boolean mStarted;

    interface ObjectJsonReader<O> {
        O read(JsonReader reader) throws IOException;
    }

    public JsonArrayIterator(Reader reader, ObjectJsonReader<T> objectReader) {
        mJsonReader = new JsonReader(reader);
        mObjectReader = objectReader;
    }

    public void close() throws IOException {
        mJsonReader.close();
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        try {
            if (!mStarted) {
                mJsonReader.beginArray();
                mStarted = true;
            }
            hasNext = mJsonReader.hasNext();

            if (!hasNext) {
                mJsonReader.endArray();
            }
        } catch (IOException e) {
            AppLog.e(e);
        }

        return hasNext;
    }

    @Override
    public T next() {
        T object = null;
        try {
            object = mObjectReader.read(mJsonReader);
        } catch (IOException e) {
            AppLog.e(e);
        }

        return object;
    }

    @Override
    public void remove() {

    }
}
