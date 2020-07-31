//------------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation. All rights reserved.
//------------------------------------------------------------------------------
package com.anod.appwatcher.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import androidx.window.DisplayFeature
import androidx.window.WindowManager
import info.anodsplace.framework.AppLog

interface HingeDevice {
    val hinge: Rect

    class NoOp : HingeDevice {
        override val hinge = Rect()
    }

    companion object {
        private fun isDuo(context: Context) = context.packageManager.hasSystemFeature("com.microsoft.device.display.displaymask")
        fun create(activity: Activity): HingeDevice = if (isDuo(activity.applicationContext))
            HingeDeviceReal(activity)
        else
            NoOp()
    }
}

class HingeDeviceReal(activity: Activity) : HingeDevice {
    private val xWindowManager = WindowManager(activity, null)

    override val hinge: Rect
        get() {
            try {
                val hinge = xWindowManager.windowLayoutInfo.displayFeatures.firstOrNull { it.type == DisplayFeature.TYPE_HINGE }
                return hinge?.bounds ?: Rect()
            } catch (e: Exception) {
                AppLog.e(e)
            }
            return Rect()
        }
}