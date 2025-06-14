package com.anod.appwatcher.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.app.HingeDevice

abstract class BaseComposeActivity : ComponentActivity() {
    lateinit var hingeDevice: HingeDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        hingeDevice = HingeDevice.create(this)
        super.onCreate(savedInstanceState)
    }
}