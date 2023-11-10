package com.anod.appwatcher.backup.gdrive

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.sync.SyncNotification
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.services.drive.DriveScopes
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.NotificationManager
import info.anodsplace.framework.playservices.GoogleSignInConnect
import org.koin.java.KoinJavaComponent
import java.util.Collections
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal fun createGDriveSignInOptions(): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .requestEmail()
            .build()
}

internal fun createCredentials(context: Context, googleAccount: GoogleSignInAccount): HttpRequestInitializer {
    return GoogleAccountCredential
            .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA))
            .setSelectedAccount(googleAccount.account)
}

interface ResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}

class GDriveSignIn(private val activity: Activity, private val listener: Listener) : ResultListener {

    private val driveConnect by lazy { GoogleSignInConnect(activity, createGDriveSignInOptions()) }

    companion object {
        const val resultCodeGDriveSignIn = 123
        const val resultCodeGDriveException = 124

        fun showResolutionNotification(resolution: PendingIntent, context: ApplicationContext) {
            val notification = NotificationCompat.Builder(context.actual, SyncNotification.authenticationId).apply {
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(context.getString(R.string.google_drive_sync_failed))
                setContentText(context.getString(R.string.google_drive_sync_action))
                setContentIntent(resolution)
            }.build()
            val notificationManager = KoinJavaComponent.getKoin().get<NotificationManager>()
            notificationManager.notify(SyncNotification.gmsNotificationId, notification)
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

    suspend fun signOut() = suspendCoroutine<Unit> { continuation ->
        driveConnect.disconnect(object : GoogleSignInConnect.SignOutResult {
            override fun onResult() {
                continuation.resume(Unit)
            }
        })
    }

    fun requestEmail(lastSignedAccount: GoogleSignInAccount) {
        GoogleSignIn.requestPermissions(activity, resultCodeGDriveSignIn, lastSignedAccount, Scope("email"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == resultCodeGDriveSignIn) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else if (requestCode == resultCodeGDriveException) {
            // Nothing?
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!

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

        try {
            return driveConnect.connectLocked()
        } catch (e: ApiException) {
            val errorCode = e.statusCode
            AppLog.e("Silent sign in failed with code $errorCode (${GoogleSignInStatusCodes.getStatusCodeString(errorCode)}). starting signIn intent")
            if (errorCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
                val settingActivity = Intent(context.actual, SettingsActivity::class.java)
                settingActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                GDriveSignIn.showResolutionNotification(
                        PendingIntent.getActivity(context.actual, 0, settingActivity, PendingIntent.FLAG_IMMUTABLE), context)
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