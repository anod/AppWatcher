package com.anod.appwatcher.compose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.HingeDevice

abstract class BaseComposeActivity : AppCompatActivity() {
    lateinit var hingeDevice: HingeDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.appCompatNightMode)
        hingeDevice = HingeDevice.create(this)
        super.onCreate(savedInstanceState)
    }
}