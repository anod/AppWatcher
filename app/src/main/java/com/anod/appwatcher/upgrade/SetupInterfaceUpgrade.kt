package com.anod.appwatcher.upgrade

import android.content.Context
import android.content.Intent
import android.widget.Switch
import com.anod.appwatcher.R
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.DialogCustom


/**
 * @author Alex Gavrishev
 * @date 02-Mar-18
 */
class SetupInterfaceUpgrade(val prefs: Preferences, val context: Context): UpgradeTask {

    override fun onUpgrade(upgrade: UpgradeCheck.Result) {
        if (upgrade.oldVersionCode > 90) {
            return
        }

        DialogCustom(context, R.style.AppTheme_Dialog, R.string.setup_interface_title, R.layout.dialog_setup_interface) { view, builder ->
            var recreate = false
            view.findViewById<Switch>(R.id.recentToggle).isChecked = prefs.showRecent
            view.findViewById<Switch>(R.id.onDeviceToggle).isChecked = prefs.showOnDevice
            view.findViewById<Switch>(R.id.recentlyUpdatedToggle).isChecked = prefs.showRecentlyUpdated

            view.findViewById<Switch>(R.id.recentToggle).setOnCheckedChangeListener { _, checked ->
                prefs.showRecent = checked
                recreate = true
            }

            view.findViewById<Switch>(R.id.onDeviceToggle).setOnCheckedChangeListener { _, checked ->
                prefs.showOnDevice = checked
                recreate = true
            }

            view.findViewById<Switch>(R.id.recentlyUpdatedToggle).setOnCheckedChangeListener { _, checked ->
                prefs.showRecentlyUpdated = checked
                recreate = true
            }

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                if (recreate) {
                    val i = Intent(context, AppWatcherActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(i)
                }
                dialog.dismiss()
            }
        }.show()
    }
}