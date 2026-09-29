// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.PackageStateCache
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.clearDisabledUpdateStatus
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PackageRemovedReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val packageName = intent.data?.schemeSpecificPart ?: return
        when (action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                handlePackageChange(packageName, clearDisabledUpdates = false, requireStateChange = false)
            }
            Intent.ACTION_PACKAGE_ADDED -> {
                handlePackageChange(packageName, clearDisabledUpdates = true, requireStateChange = false)
            }
            Intent.ACTION_PACKAGE_CHANGED -> {
                if (isApplicationPackageChange(packageName, intent.getStringArrayExtra(Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST))) {
                    handlePackageChange(packageName, clearDisabledUpdates = true, requireStateChange = true)
                }
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                handlePackageChange(packageName, clearDisabledUpdates = true, requireStateChange = false)
            }
        }
    }

    private fun handlePackageChange(
        packageName: String,
        clearDisabledUpdates: Boolean,
        requireStateChange: Boolean
    ) {
        val pendingResult = goAsync()
        appScope.launch {
            try {
                val packageStateRefresh = get<PackageStateCache>().refresh(packageName)
                if (clearDisabledUpdates) {
                    get<AppsDatabase>().apps().clearDisabledUpdateStatus(
                        packageName = packageName,
                        packageState = packageStateRefresh.current
                    )
                }
                if (!requireStateChange || packageStateRefresh.changed) {
                    get<PackageChangedReceiver>().emit(packageName + ":" + System.currentTimeMillis())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

internal fun isApplicationPackageChange(
    packageName: String,
    changedComponents: Array<String>?
): Boolean = changedComponents == null || packageName in changedComponents