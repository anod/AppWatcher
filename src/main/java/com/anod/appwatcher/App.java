package com.anod.appwatcher;

import android.content.Context;

/**
 * @author algavris
 * @date 07/05/2016.
 */
public class App {

    public static AppWatcherApplication with(Context context)
    {
        return AppWatcherApplication.get(context);
    }

    public static ObjectGraph provide(Context context)
    {
        return AppWatcherApplication.provide(context);
    }

}
