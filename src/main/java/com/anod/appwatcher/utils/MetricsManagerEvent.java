package com.anod.appwatcher.utils;

import android.content.Context;
import android.os.Bundle;

import com.anod.appwatcher.App;

/**
 * @author algavris
 * @date 03/09/2016.
 */
public class MetricsManagerEvent {
    public static void track(Context context, String eventName, String... params) {
        Bundle bundle = new Bundle();

        for (int i = 0; i < params.length; i = i + 2)
        {
            bundle.putString(params[i], params[i+1]);
        }

        App.provide(context).firebase().logEvent(eventName, bundle);
    }
}
