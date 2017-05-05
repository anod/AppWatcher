package com.anod.appwatcher.utils;

import com.anod.appwatcher.BuildConfig;

/**
 * @author algavris
 * @date 02/05/2017.
 */

public class Assert {

    public static void expr(boolean expr) {
        if (BuildConfig.DEBUG) {
            if (!expr) throw new AssertionError();
        }
    }
}
