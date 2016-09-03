package com.anod.appwatcher.utils;

import android.support.v4.util.SimpleArrayMap;

import net.hockeyapp.android.CrashManagerListener;

/**
 * @author algavris
 * @date 03/09/2016.
 */
public class AppCrashListener extends CrashManagerListener {
    private SimpleArrayMap<String, String> mDescription = new SimpleArrayMap<>();

    public void put(String key, String value)
    {
        mDescription.put(key, value);
    }

    @Override
    public String getDescription() {
        if (mDescription.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mDescription.size(); i++) {
            String key = mDescription.keyAt(i);
            String value = mDescription.valueAt(i);
            sb.append(key).append(": ").append(value).append("\n");
        }
        return sb.toString();
    }
}
