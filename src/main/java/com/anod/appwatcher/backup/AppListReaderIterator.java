package com.anod.appwatcher.backup;

import com.android.util.JsonReader;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppLog;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * Created by alex on 7/26/14.
 */
public class AppListReaderIterator implements Iterator<AppInfo> {
    private JsonReader mJsonReader;
    private boolean mStarted;
    private AppListReader mAppInfoReader;

    public AppListReaderIterator(Reader reader) {
        mJsonReader = new JsonReader(reader);
        mAppInfoReader = new AppListReader();
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
            AppLog.ex(e);
        }

        return hasNext;
    }

    @Override
    public AppInfo next() {
        AppInfo info = null;
        try {
            info = mAppInfoReader.readAppInfo(mJsonReader);
        } catch (IOException e) {
            AppLog.ex(e);
        }

        return info;
    }

    @Override
    public void remove() {

    }
}
