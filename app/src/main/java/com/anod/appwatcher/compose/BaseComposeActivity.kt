package com.anod.appwatcher.compose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.anod.appwatcher.utils.Theme
import com.google.android.material.color.DynamicColors
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.HingeDevice
import org.koin.java.KoinJavaComponent.getKoin

abstract class BaseComposeActivity : AppCompatActivity() {
    lateinit var hingeDevice: HingeDevice

    val theme: Theme
        get() = Theme(this, getKoin().get())

    open val themeColors: CustomThemeColors
        get() = theme.colors

    private val themeRes: Int
        get() = if (themeColors.statusBarColor.isLight)
            theme.themeLightActionBar
        else
            theme.themeDarkActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.appCompatNightMode)
        hingeDevice = HingeDevice.create(this)
        super.onCreate(savedInstanceState)
        setTheme(this.themeRes)
        DynamicColors.applyToActivityIfAvailable(this)
    }
}