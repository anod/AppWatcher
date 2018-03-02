package com.anod.appwatcher.upgrade

import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.preferences.Preferences

/**
 * @author algavris
 * *
 * @date 08/10/2016.
 */

class UpgradeCheck(private val preferences: Preferences) {

    class Result(val isNewVersion: Boolean, val oldVersionCode: Int)

    val result: Result
        get() {
            val code = preferences.versionCode
            if (code == 0) {
                preferences.versionCode = BuildConfig.VERSION_CODE
                return Result(false, 0)
            }

            if (code < BuildConfig.VERSION_CODE) {
                preferences.versionCode = BuildConfig.VERSION_CODE
                return Result(true, code)
            }
            return Result(false, code)
        }
}
