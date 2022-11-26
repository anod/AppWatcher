package com.anod.appwatcher.utils

import android.content.Context
import android.content.res.Configuration
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.CustomThemeColor
import info.anodsplace.framework.app.CustomThemeColors

/**
 * @author Alex Gavrishev
 * @date 07/12/2017
 */
class Theme(private val context: Context, private val prefs: Preferences) {

    val theme: Int
        get() {
            if (isNightMode) {
                if (prefs.theme == Preferences.THEME_BLACK) {
                    return R.style.AppTheme_Black
                }
            }
            return R.style.AppTheme_Main
        }

    val colors: CustomThemeColors
        get() = isNightMode.let { isNightMode ->
            if (prefs.theme == Preferences.THEME_BLACK)
                CustomThemeColors(CustomThemeColor.black, CustomThemeColor.black)
            else
                CustomThemeColors(CustomThemeColor(colorAttr = android.R.attr.colorBackground, isLight = !isNightMode), CustomThemeColor(colorAttr = android.R.attr.colorBackground, isLight = !isNightMode))
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

    val isNightMode: Boolean
        get() {
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> return true
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
            }
            return false
        }
}