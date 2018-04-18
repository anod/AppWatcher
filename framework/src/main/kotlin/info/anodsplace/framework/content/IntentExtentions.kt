package info.anodsplace.framework.content

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import info.anodsplace.framework.AppLog

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

