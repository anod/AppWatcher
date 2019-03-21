package info.anodsplace.framework.app

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

object WindowCustomTheme {

    fun apply(themeColors: CustomThemeColors, window: Window, activity: Activity) {
        var systemUiVisibility = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                if (themeColors.statusBarColor.available) {
                    window.statusBarColor = themeColors.statusBarColor.get(activity)
                    if (themeColors.statusBarColor.isLight) {
                        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
                if (themeColors.navigationBarColor.available) {
                    window.navigationBarColor = themeColors.navigationBarColor.get(activity)
                    if (themeColors.navigationBarColor.isLight) {
                        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                }
                window.decorView.systemUiVisibility = systemUiVisibility
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (themeColors.statusBarColor.available) {
                    window.statusBarColor = themeColors.statusBarColor.get(activity)
                    if (themeColors.statusBarColor.isLight) {
                        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
                window.decorView.systemUiVisibility = systemUiVisibility
            }
        }
    }
}