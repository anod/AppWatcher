// Copyright (c) 2020. Alex Gavrishev
package info.anodsplace.framework.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build

val Activity.isMultiWindow: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) isInMultiWindowMode else false

val Context.isMultiWindow: Boolean
    get() = if (this is Activity) isMultiWindow else false

fun Intent.addMultiWindowFlags(context: Context): Intent {
    if (context.isMultiWindow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_DOCUMENT) // or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
    }
    return this
}