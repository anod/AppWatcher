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
        if (action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart ?: ""
            val component = context?.provide ?: return
            component.appScope.launch {
                component.packageRemovedReceiver.emit(packageName + ":" + System.currentTimeMillis())
            }
        }
    }
}