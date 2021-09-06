// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anod.appwatcher.provide
import kotlinx.coroutines.launch

class PackageRemovedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                notify(context, intent)
            }
            Intent.ACTION_PACKAGE_ADDED -> {
                notify(context, intent)
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                notify(context, intent)
            }
        }
    }

    private fun notify(context: Context?, intent: Intent?) {
        val packageName = intent?.data?.schemeSpecificPart ?: ""
        val component = context?.provide ?: return
        component.appScope.launch {
            component.packageChangedReceiver.emit(packageName + ":" + System.currentTimeMillis())
        }
    }
}