package com.anod.appwatcher.upgrade

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author Alex Gavrishev
 * @date 02-Mar-18
 */
class UpgradeRefresh(val prefs: Preferences, val activity: Activity, private val scope: CoroutineScope) : UpgradeTask {
    override fun onUpgrade(upgrade: UpgradeCheck.Result) {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(activity)
        if (prefs.isDriveSyncEnabled) {
            val gDrive = GDriveSignIn(activity, object : GDriveSignIn.Listener {
                override fun onGDriveLoginSuccess(googleSignInAccount: GoogleSignInAccount) {
                    requestRefresh()
                }

                override fun onGDriveLoginError(errorCode: Int) {
                    val settingActivity = Intent(activity, SettingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    GDriveSignIn.showResolutionNotification(
                            PendingIntent.getActivity(activity, 0, settingActivity, 0), ApplicationContext(activity))
                }
            })
            when {
                googleAccount == null -> {
                    Toast.makeText(activity, activity.getString(R.string.refresh_gdrive_mesage), Toast.LENGTH_LONG).show()
                    gDrive.signIn()
                }
                googleAccount.account == null -> {
                    Toast.makeText(activity, activity.getString(R.string.refresh_gdrive_mesage), Toast.LENGTH_LONG).show()
                    gDrive.requestEmail(googleAccount)
                }
                else -> requestRefresh()
            }
        } else {
            requestRefresh()
        }
    }

    private fun requestRefresh() {
        scope.launch {
            val scheduler = SyncScheduler(activity)
            scheduler.execute().collect { }
            if (prefs.useAutoSync) {
                scheduler
                    .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), true)
                    .collect { }
            }
        }
    }
}