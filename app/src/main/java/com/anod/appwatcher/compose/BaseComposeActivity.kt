package com.anod.appwatcher.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import info.anodsplace.framework.app.FoldableDevice

abstract class BaseComposeActivity : ComponentActivity() {
    lateinit var foldableDevice: FoldableDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        foldableDevice = FoldableDevice.create(this)
        super.onCreate(savedInstanceState)
    }
}