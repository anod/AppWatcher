package com.anod.appwatcher.upgrade

import android.content.Context
import android.content.Intent
import android.widget.Switch
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.DialogCustom


/**
 * @author algavris
 * @date 02-Mar-18
 */
class SetupInterfaceUpgrade(val prefs: Preferences, val context: Context): UpgradeTask {

    override fun onUpgrade(upgrade: UpgradeCheck.Result) {
        if (upgrade.oldVersionCode > 90) {
            return
        }

        DialogCustom(context, R.string.setup_interface_title, R.layout.dialog_setup_interface, { view, builder ->
                view.findViewById<Switch>(R.id.recentToggle).setOnCheckedChangeListener({ _, checked ->
                    prefs.showRecent = checked
                })

                view.findViewById<Switch>(R.id.onDeviceToggle).setOnCheckedChangeListener({ _, checked ->
                    prefs.showOnDevice = checked
                })

                builder.setPositiveButton(android.R.string.ok, { dialog, which ->
                    val i = Intent(context, AppWatcherActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(i)
                    dialog.dismiss()
                })
        }).show()
    }
}