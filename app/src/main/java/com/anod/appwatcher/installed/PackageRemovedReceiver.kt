// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.clearDisabledUpdateStatus
import com.anod.appwatcher.utils.isPackageEnabled
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PackageRemovedReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                notify(intent, clearDisabledUpdates = false)
            }
            Intent.ACTION_PACKAGE_ADDED -> {
                notify(intent, clearDisabledUpdates = true)
            }
            Intent.ACTION_PACKAGE_CHANGED -> {
                notify(intent, clearDisabledUpdates = true)
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                notify(intent, clearDisabledUpdates = true)
            }
        }
    }

    private fun notify(intent: Intent, clearDisabledUpdates: Boolean) {
        val packageName = intent.data?.schemeSpecificPart ?: ""
        val pendingResult = goAsync()
        appScope.launch {
            try {
                if (clearDisabledUpdates) {
                    val packageManager = get<PackageManager>()
                    get<AppsDatabase>().apps().clearDisabledUpdateStatus(
                        packageName = packageName,
                        installedApps = InstalledApps.PackageManager(packageManager),
                        packageEnabled = packageManager::isPackageEnabled
                    )
                }
                get<PackageChangedReceiver>().emit(packageName + ":" + System.currentTimeMillis())
            } finally {
                pendingResult.finish()
            }
        }
    }
}