// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anod.appwatcher.provide

class PackageRemovedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart ?: ""
            context?.provide?.packageRemovedReceiver?.offer(packageName + ":" + System.currentTimeMillis())
        }
    }
}