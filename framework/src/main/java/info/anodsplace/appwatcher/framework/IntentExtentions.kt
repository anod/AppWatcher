package info.anodsplace.appwatcher.framework

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import info.anodsplace.android.log.AppLog

fun Intent.forPlayStore(pkg: String): Intent {
    val url = String.format(MarketInfo.URL_PLAY_STORE, pkg)
    this.action = Intent.ACTION_VIEW
    this.data = Uri.parse(url)
    this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    return this
}

fun Intent.forMyApps(update: Boolean): Intent {
    this.action = "com.google.android.finsky.VIEW_MY_DOWNLOADS"
    this.component = ComponentName("com.android.vending",
            "com.google.android.finsky.activities.MainActivity")
    if (update) {
        this.putExtra("trigger_update_all", true)
    }
    return this
}

fun Intent.forUninstall(packageName: String): Intent {
    this.action = Intent.ACTION_UNINSTALL_PACKAGE
    this.data = Uri.fromParts("package", packageName, null)
    return this
}

fun Context.startActivitySafely(intent: Intent) {
    try {
        this.startActivity(intent)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(this, "Cannot start activity: " + intent.toString(), Toast.LENGTH_SHORT).show()
    }
}

