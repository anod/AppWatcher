// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.view.View

fun View.reveal(animate: Boolean, startDelay: Long = 0L, duration: Long = 200L, fromAlpha: Float = 0f) {
    if (animate) {
        alpha = fromAlpha
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

fun View.hide(animate: Boolean, startDelay: Long = 0L, duration: Long = 200L) {
    if (animate) {
        alpha = 1f
        visibility = View.VISIBLE
        animate()
                .setStartDelay(startDelay)
                .alpha(0f)
                .setDuration(duration)
                .setListener(null)
    } else {
        alpha = 1f
        visibility = View.GONE
    }
}