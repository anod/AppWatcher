package com.anod.appwatcher.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

abstract class BaseComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
    }
}

fun ComponentActivity.setEdgeToEdgeConfig() {
    enableEdgeToEdge()
    // Force the 3-button navigation bar to be transparent
    // See: https://developer.android.com/develop/ui/views/layout/edge-to-edge#create-transparent
    window.isNavigationBarContrastEnforced = false
}