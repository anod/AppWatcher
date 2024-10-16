package com.anod.appwatcher.upgrade

import android.app.UiModeManager
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Alex Gavrishev
 * @date 18/04/2018
 */
class Upgrade15500: UpgradeTask, KoinComponent {
    private val prefs: Preferences by inject()
    private val appCoroutineScope: CoroutineScope by inject()
    private val database: AppsDatabase by inject()

    override fun onUpgrade(upgrade: UpgradeCheck.Result) {
        if (upgrade.oldVersionCode > 15500) {
            return
        }

        prefs.uiMode = UiModeManager.MODE_NIGHT_NO
        appCoroutineScope.launch {
            AppTagsTable.Queries.clean(database)
        }
    }
}