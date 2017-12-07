package com.anod.appwatcher.utils

import android.app.Activity
import android.content.res.Configuration
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences

/**
 * @author algavris
 * @date 07/12/2017
 */
class Theme(private val activity: Activity) {

    val theme: Int
        get() {
            if (isNightMode) {
                if (App.provide(activity).prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Black
                }
            }
            return R.style.AppTheme_Main
        }

    val themeDialog: Int
        get() {
            if (isNightMode) {
                if (App.provide(activity).prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Dialog_Black
                }
            }
            return R.style.AppTheme_Dialog
        }

    val themeChangelog: Int
        get() {
            if (isNightMode) {
                if (App.provide(activity).prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Dialog_Black_Changelog
                }
            }
            return R.style.AppTheme_Dialog_Changelog
        }

    val isNightTheme: Boolean
        get() = isNightMode

    private val isNightMode: Boolean
        get() {
            val currentNightMode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_YES -> return true
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
            }
            return false
        }
}