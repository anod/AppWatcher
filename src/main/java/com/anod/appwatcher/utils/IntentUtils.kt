package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import com.anod.appwatcher.market.MarketInfo

import info.anodsplace.android.log.AppLog

object IntentUtils {
    private const val SCHEME = "package"

    fun createPlayStoreIntent(pkg: String): Intent {
        val url = String.format(MarketInfo.URL_PLAY_STORE, pkg)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        return intent
    }

    fun createMyAppsIntent(update: Boolean): Intent {
        val marketIntent = Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS")
                .setComponent(ComponentName("com.android.vending",
                        "com.google.android.finsky.activities.MainActivity"))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (update) {
            marketIntent.putExtra("trigger_update_all", true)
        }
        return marketIntent
    }

    fun createUninstallIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.fromParts(SCHEME, packageName, null))
    }

    fun startActivitySafely(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLog.e(e)
            Toast.makeText(context, "Cannot start activity: " + intent.toString(), Toast.LENGTH_SHORT).show()
        }

    }
}
