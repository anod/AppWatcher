package com.anod.appwatcher.utils

import com.anod.appwatcher.BuildConfig

/**
 * @author algavris
 * *
 * @date 02/05/2017.
 */

object Assert {

    fun expr(expr: Boolean) {
        if (BuildConfig.DEBUG) {
            if (!expr) throw AssertionError()
        }
    }
}
