// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.view.View

fun View.reveal(animate: Boolean, startDelay: Long = 0L, duration: Long = 200L) {
    if (animate) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
                .setStartDelay(startDelay)
                .alpha(1f)
                .setDuration(duration)
                .setListener(null)
    } else {
        alpha = 1f
        visibility = View.VISIBLE
    }
}