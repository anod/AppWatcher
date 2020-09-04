//------------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation. All rights reserved.
//------------------------------------------------------------------------------
package com.anod.appwatcher.utils

import android.app.Activity
import android.graphics.Rect
import androidx.window.DisplayFeature
import androidx.window.WindowManager
import info.anodsplace.framework.AppLog

interface HingeDevice {
    val hinge: Rect
    var attachedToWindow: Boolean

    class NoOp : HingeDevice {
        override var attachedToWindow = false
        override val hinge = Rect()
    }

    companion object {
        // private fun isDuo(context: Context) = context.packageManager.hasSystemFeature("com.microsoft.device.display.displaymask")
        fun create(activity: Activity): HingeDevice = HingeDeviceReal(activity)
    }
}

class HingeDeviceReal(activity: Activity) : HingeDevice {
    override var attachedToWindow = false

    private val xWindowManager: WindowManager? = try {
        WindowManager(activity, null)
    } catch (e: Exception) {
        null
    }

    override val hinge: Rect
        get() {
            if (!attachedToWindow) {
                return Rect()
            }
            val wm = xWindowManager ?: return Rect()
            try {
                val hinge = wm.windowLayoutInfo.displayFeatures.firstOrNull { it.type == DisplayFeature.TYPE_HINGE }
                return hinge?.bounds ?: Rect()
            } catch (e: Exception) {
                AppLog.e(e)
            }
            return Rect()
        }
}