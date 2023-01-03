package com.anod.appwatcher.utils

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build

/**
 * @author Alex Gavrishev
 * @date 14/02/2018
 */
object StoreIntent {
    const val URL_PLAY_STORE = "market://details?id=%s"
    const val URL_WEB_PLAY_STORE = "https://play.google.com/store/apps/details?id=%s"
}

fun Intent.forPlayStore(pkg: String): Intent {
    val url = String.format(StoreIntent.URL_PLAY_STORE, pkg)
    this.action = Intent.ACTION_VIEW
    this.data = Uri.parse(url)
    return this
}

fun Intent.forMyApps(update: Boolean): Intent {
    action = "com.google.android.finsky.VIEW_MY_DOWNLOADS"
    component = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        ComponentName(
                "com.android.vending",
                "com.android.vending.AssetBrowserActivity"
        )
    else ComponentName(
            "com.android.vending",
            "com.google.android.finsky.activities.MainActivity"
    )
    if (update) {
        this.putExtra("trigger_update_all", true)
    }
    return this
}