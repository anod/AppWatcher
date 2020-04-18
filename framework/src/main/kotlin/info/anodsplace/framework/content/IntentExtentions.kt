package info.anodsplace.framework.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.Fragment

import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.addMultiWindowFlags

fun Intent.forUninstall(packageName: String): Intent {
    this.action = Intent.ACTION_UNINSTALL_PACKAGE
    this.data = Uri.fromParts("package", packageName, null)
    return this
}

fun Intent.forAppInfo(packageName: String, context: Context): Intent {
    this.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    this.data = Uri.fromParts("package", packageName, null)
    addMultiWindowFlags(context)
    return this
}

fun Context.startActivitySafely(intent: Intent) {
    try {
        this.startActivity(intent)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(this, "Cannot start activity: $intent", Toast.LENGTH_SHORT).show()
    }
}

fun Fragment.startActivitySafely(intent: Intent) {
    try {
        requireActivity().startActivity(intent)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(requireContext(), "Cannot start activity: $intent", Toast.LENGTH_SHORT).show()
    }
}

fun Activity.startActivityForResultSafely(intent: Intent, requestCode: Int) {
    try {
        this.startActivityForResult(intent, requestCode)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(this, "Cannot start activity: $intent", Toast.LENGTH_SHORT).show()
    }
}
