package com.anod.appwatcher.utils

import android.app.Activity
import android.content.res.Configuration
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.CustomThemeColor
import info.anodsplace.framework.app.CustomThemeColors

/**
 * @author Alex Gavrishev
 * @date 07/12/2017
 */
class Theme(private val activity: Activity, private val prefs: Preferences) {

    val theme: Int
        get() {
            if (isNightMode) {
                if (prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Black
                }
            }
            return R.style.AppTheme_Main
        }

    val themeLightActionBar: Int
        get() {
            if (isNightMode) {
                if (prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Black_LightActionBar
                }
            }
            return R.style.AppTheme_Main_LightActionBar
        }

    val themeDarkActionBar: Int
        get() {
            if (isNightMode) {
                if (prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Black
                }
                R.style.AppTheme_Main
            }
            return R.style.AppTheme_Main_DarkActionBar
        }

    val colors: CustomThemeColors
        get() =
            when (isNightMode) {
                false ->
                    CustomThemeColors(CustomThemeColor.white, CustomThemeColor.white)
                prefs.theme == Preferences.THEME_BLACK ->
                    CustomThemeColors(CustomThemeColor.black, CustomThemeColor.black)
                else ->
                    CustomThemeColors(CustomThemeColor(0, R.color.dark_primary_base, false), CustomThemeColor(0, R.color.dark_primary_base, false))
            }

    val themeDialog: Int
        get() {
            if (isNightMode) {
                if (prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Dialog_Black
                }
            }
            return R.style.AppTheme_Dialog
        }

    val themeDialogNoActionBar: Int
        get() {
            if (isNightMode) {
                if (prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Dialog_NoActionBar_Black
                }
            }
            return R.style.AppTheme_Dialog_NoActionBar
        }

    val isNightTheme: Boolean
        get() = isNightMode

    private val isNightMode: Boolean
        get() {
            when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> return true
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
            }
            return false
        }
}