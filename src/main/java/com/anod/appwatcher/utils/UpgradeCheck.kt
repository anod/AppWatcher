package com.anod.appwatcher.utils

import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.Preferences

/**
 * @author algavris
 * *
 * @date 08/10/2016.
 */

class UpgradeCheck(private val preferences: Preferences) {

    val isNewVersion: Boolean
        get() {
            val code = preferences.versionCode
            if (code > BuildConfig.VERSION_CODE) {
                preferences.versionCode = BuildConfig.VERSION_CODE
                return true
            }

            if (code == 0) {
                preferences.versionCode = BuildConfig.VERSION_CODE
            }
            return false
        }
}
