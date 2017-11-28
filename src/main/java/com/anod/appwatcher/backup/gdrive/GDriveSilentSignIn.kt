package com.anod.appwatcher.backup.gdrive

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.framework.ApplicationContext
import com.anod.appwatcher.framework.GoogleSignInConnect
import com.anod.appwatcher.sync.SyncNotification
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.drive.Drive
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * @date 28/11/2017
 */

internal fun createGDriveSignInOptions(): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_APPFOLDER)
            .build()
}

class GDriveSilentSignIn(private val context: ApplicationContext) {

    private val driveConnect by lazy { GoogleSignInConnect(context, createGDriveSignInOptions()) }

    fun showResolutionNotification(resolution: PendingIntent) {
        val builder = NotificationCompat.Builder(context.actual, NotificationChannel.DEFAULT_CHANNEL_ID)
        builder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.google_drive_sync_failed))
                .setContentText(context.getString(R.string.user_action_required))
                .setContentIntent(resolution)

        val notification = builder.build()
        context.notificationManager.notify(SyncNotification.gpsNotificationId, notification)
    }

    fun signInLocked(): GoogleSignInAccount {

        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context.actual)
        if (lastSignedInAccount != null) {
            return lastSignedInAccount
        }

        try{
            return driveConnect.connectLocked()
        } catch (e: ApiException) {
            val errorCode = e.statusCode
            AppLog.e("Silent sign in failed with code $errorCode (${GoogleSignInStatusCodes.getStatusCodeString(errorCode)}). starting signIn intent")
            if (errorCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
                val settingActivity = Intent(context.actual, SettingsActivity::class.java)
                settingActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                showResolutionNotification(PendingIntent.getActivity(context.actual, 0, settingActivity, 0))
            }
            throw Exception("Google drive account is null", e)
        }
    }

}