package com.anod.appwatcher.utils

import android.app.Activity
import android.content.res.Configuration
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.CustomThemeColors

/**
 * @author Alex Gavrishev
 * @date 07/12/2017
 */
class Theme(private val activity: Activity) {

    val theme: Int
        get() {
            if (isNightMode) {
                if (Application.provide(activity).prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Black
                }
            }
            return R.style.AppTheme_Main
        }

    val colors: CustomThemeColors
        get() =
            when (isNightMode){
                false ->
                    CustomThemeColors(true, R.color.material_grey_50_, R.color.material_grey_50_)
                Application.provide(activity).prefs.theme == Preferences.THEME_BLACK ->
                    CustomThemeColors(false, android.R.color.black, android.R.color.black)
                else ->
                    CustomThemeColors(true, R.color.material_grey_900, R.color.material_grey_900)
            }

    val themeDialog: Int
        get() {
            if (isNightMode) {
                if (Application.provide(activity).prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Dialog_Black
                }
            }
            return R.style.AppTheme_Dialog
        }

    val themeChangelog: Int
        get() {
            if (isNightMode) {
                if (Application.provide(activity).prefs.theme == Preferences.THEME_BLACK) {
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