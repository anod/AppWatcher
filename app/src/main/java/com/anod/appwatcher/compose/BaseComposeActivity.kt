package com.anod.appwatcher.compose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import info.anodsplace.framework.app.FoldableDevice

abstract class BaseComposeActivity : ComponentActivity() {
    lateinit var foldableDevice: FoldableDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        foldableDevice = FoldableDevice.create(this)
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
    }
}

fun ComponentActivity.setEdgeToEdgeConfig() {
    enableEdgeToEdge()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Force the 3-button navigation bar to be transparent
        // See: https://developer.android.com/develop/ui/views/layout/edge-to-edge#create-transparent
        window.isNavigationBarContrastEnforced = false
    }
}