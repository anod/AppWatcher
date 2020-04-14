//------------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation. All rights reserved.
//------------------------------------------------------------------------------
package com.anod.appwatcher.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.WindowManager
import com.microsoft.device.display.DisplayMask

interface DuoDevice {
    val hinge: Rect

    class NoOp : DuoDevice {
        override val hinge = Rect()
    }

    companion object {
        private fun isDuo(context: Context) = context.packageManager.hasSystemFeature("com.microsoft.device.display.displaymask")
        fun create(activity: Activity): DuoDevice = if (isDuo(activity.applicationContext))
            DuoDeviceReal(activity)
        else
            NoOp()
    }
}

class DuoDeviceReal(private val activity: Activity) : DuoDevice {
    private val rotation: Int
        get() = getRotation(activity)

    override val hinge: Rect
        get() = getDeviceHinge(rotation)

    private fun getDeviceHinge(rotation: Int): Rect {
        val displayMask = DisplayMask.fromResourcesRectApproximation(activity)
        val bounding = displayMask?.getBoundingRectsForRotation(rotation) ?: emptyList()
        if (bounding.isEmpty()) {
            return Rect()
        }
        return bounding[0]
    }

    companion object {
        fun getRotation(activity: Activity): Int {
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return wm.defaultDisplay.rotation
        }
    }
}