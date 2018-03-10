package com.anod.appwatcher.backup.gdrive

import android.app.Activity
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.sync.SyncNotification
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.drive.Drive
import com.google.android.gms.tasks.Task
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ActivityListener
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.playservices.GoogleSignInConnect
import java.util.concurrent.ExecutionException

internal fun createGDriveSignInOptions(): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_APPFOLDER)
            .build()
}

class GDriveSignIn(private val activity: Activity, private val listener: Listener): ActivityListener.ResultListener {

    private val driveConnect by lazy { GoogleSignInConnect(activity, createGDriveSignInOptions()) }

    companion object {
        const val resultCodeGDriveSignIn = 123

        fun showResolutionNotification(resolution: PendingIntent, context: ApplicationContext) {
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
    }

    interface Listener {
        fun onGDriveLoginSuccess(googleSignInAccount: GoogleSignInAccount)
        fun onGDriveLoginError(errorCode: Int)
    }

    fun signIn() {
        driveConnect.connect(object : GoogleSignInConnect.Result {
            override fun onSuccess(account: GoogleSignInAccount, client: GoogleSignInClient) {
                listener.onGDriveLoginSuccess(account)
            }

            override fun onError(errorCode: Int, client: GoogleSignInClient) {
                AppLog.e("Silent sign in failed with code $errorCode (${GoogleSignInStatusCodes.getStatusCodeString(errorCode)}). starting signIn intent")
                activity.startActivityForResult(client.signInIntent, resultCodeGDriveSignIn)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == resultCodeGDriveSignIn) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            listener.onGDriveLoginSuccess(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            AppLog.e(e)
            listener.onGDriveLoginError(e.statusCode)
        }

    }

}

class GDriveSilentSignIn(private val context: ApplicationContext) {

    private val driveConnect by lazy { GoogleSignInConnect(context, createGDriveSignInOptions()) }


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
                GDriveSignIn.showResolutionNotification(
                        PendingIntent.getActivity(context.actual, 0, settingActivity, 0), context)
            }
            throw Exception("Google drive account is null", e)
        } catch (e: ExecutionException) {
            AppLog.e(e)
            throw Exception(e.message, e)
            // The Task failed, this is the same exception you'd get in a non-blocking
            // failure handler.
        } catch (e: InterruptedException) {
            AppLog.e(e)
            throw Exception(e.message, e)
            // An interrupt occurred while waiting for the task to complete.
        }

    }

}