package com.anod.appwatcher.upgrade

import android.content.Context
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences

/**
 * @author Alex Gavrishev
 * @date 18/04/2018
 */
class SettingsUpgrade(val prefs: Preferences, val context: Context): UpgradeTask {

    override fun onUpgrade(upgrade: UpgradeCheck.Result) {
        if (upgrade.oldVersionCode > 95) {
            return
        }
        prefs.defaultMainFilterId = Filters.ALL
    }
}