package com.anod.appwatcher.utils;

import android.support.v4.util.ArrayMap;

import net.hockeyapp.android.metrics.MetricsManager;

/**
 * @author algavris
 * @date 03/09/2016.
 */
public class MetricsManagerEvent {
    public static void track(String eventName, String... params) {
        ArrayMap<String, String> properties = new ArrayMap<>();

        for (int i = 0; i < params.length; i = i + 2)
        {
            properties.put(params[i], params[i+1]);
        }

        MetricsManager.trackEvent(eventName, properties);
    }
}
