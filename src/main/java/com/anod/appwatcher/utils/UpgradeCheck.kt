package com.anod.appwatcher.utils

import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.preferences.Preferences

/**
 * @author algavris
 * *
 * @date 08/10/2016.
 */

class UpgradeCheck(private val preferences: Preferences) {

    val isNewVersion: Boolean
        get() {
            val code = preferences.versionCode
            if (code == 0) {
                preferences.versionCode = BuildConfig.VERSION_CODE
                return false
            }

            if (code < BuildConfig.VERSION_CODE) {
                preferences.versionCode = BuildConfig.VERSION_CODE
                return true
            }
            return false
        }
}
