package com.anod.appwatcher.utils

import android.content.Context
import android.content.Intent
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.DialogMessage
import info.anodsplace.framework.content.startActivitySafely

/**
 * @author Alex Gavrishev
 * @date 10-Mar-18
 */
class UpdateAll(private val context: Context, private val prefs: Preferences) {

    fun withConfirmation() {

        if (prefs.updateAllConfirmed) {
            context.startActivitySafely(Intent().forMyApps(true))
            return
        }

        DialogMessage(context,R.style.AlertDialog, R.string.update_all, R.string.update_all_warning) {
            builder ->

            builder.setPositiveButton(R.string.i_understand) { _, _ ->
                prefs.updateAllConfirmed = true
                context.startActivitySafely(Intent().forMyApps(true))
            }

            builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            }
        }.show()
    }
}