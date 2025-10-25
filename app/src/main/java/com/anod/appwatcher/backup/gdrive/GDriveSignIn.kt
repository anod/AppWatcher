package com.anod.appwatcher.backup.gdrive

import android.accounts.Account
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.sync.SyncNotification
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.services.drive.DriveScopes
import info.anodsplace.applog.AppLog
import info.anodsplace.context.ApplicationContext
import info.anodsplace.notification.NotificationManager
import info.anodsplace.playservices.GoogleSignInConnect
import org.koin.java.KoinJavaComponent
import java.util.Collections
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal fun createGDriveSignInOptions(): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        .requestEmail()
        .build()
}

internal fun createCredentials(context: Context, googleAccount: Account?): HttpRequestInitializer {
    return GoogleAccountCredential
        .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA))
        .setSelectedAccount(googleAccount)
}

class GDriveSignIn(private val context: ApplicationContext) {

    private val driveConnect by lazy { GoogleSignInConnect(context, createGDriveSignInOptions()) }

    companion object {
        const val RESULT_CODE_GDRIVE_SIGN_IN = 123
        const val RESULT_CODE_GDRIVE_EXCEPTION = 124

        fun showResolutionNotification(resolution: PendingIntent, context: ApplicationContext) {
            val notification = NotificationCompat.Builder(context.actual, SyncNotification.AUTHENTICATION_ID).apply {
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(context.getString(R.string.google_drive_sync_failed))
                setContentText(context.getString(R.string.google_drive_sync_action))
                setContentIntent(resolution)
            }.build()
            val notificationManager = KoinJavaComponent.getKoin().get<NotificationManager>()
            notificationManager.notify(SyncNotification.GMS_NOTIFICATION_ID, notification)
        }

        fun getLastSignedInAccount(context: Context): Account? {
            return GoogleSignIn.getLastSignedInAccount(context)?.account
        }
    }

    class GoogleSignInRequestException(val intent: Intent, val resultCode: Int) : Throwable()
    class GoogleSignInFailedException(val resultCode: Int) : Throwable()

    suspend fun signIn() = suspendCoroutine { continuation ->
        driveConnect.connect(object : GoogleSignInConnect.Result {
            override fun onSuccess(account: Account) {
                continuation.resume(account)
            }

            override fun onError(errorCode: Int, errorMessage: String, signInIntent: Intent) {
                AppLog.e("Silent sign in failed with code $errorCode ($errorMessage). starting signIn intent")
                continuation.resumeWithException(GoogleSignInRequestException(signInIntent, RESULT_CODE_GDRIVE_SIGN_IN))
            }
        })
    }

    suspend fun signOut() = suspendCoroutine { continuation ->
        driveConnect.disconnect(object : GoogleSignInConnect.SignOutResult {
            override fun onResult() {
                continuation.resume(Unit)
            }
        })
    }

    suspend fun onActivityResult(resultCode: Int, data: Intent?) = suspendCoroutine { continuation ->
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (resultCode == Activity.RESULT_OK && data?.extras != null) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = completedTask.getResult(ApiException::class.java)!!
                // Signed in successfully, show authenticated UI.
                continuation.resume(account)
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                AppLog.e(e)
                continuation.resumeWithException(GoogleSignInFailedException(e.statusCode))
            }
        } else {
            continuation.resumeWithException(GoogleSignInFailedException(resultCode))
            // Nothing?
        }
    }
}

class GDriveSilentSignIn(private val context: ApplicationContext) {

    private val driveConnect by lazy { GoogleSignInConnect(context, createGDriveSignInOptions()) }

    fun signInLocked(): Account {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context.actual)
        if (lastSignedInAccount?.account != null) {
            return lastSignedInAccount.account!!
        }

        try {
            return driveConnect.connectLocked()
        } catch (e: ApiException) {
            val errorCode = e.statusCode
            AppLog.e("Silent sign in failed with code $errorCode (${GoogleSignInStatusCodes.getStatusCodeString(errorCode)}). starting signIn intent")
            if (errorCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
                val settingActivity = AppWatcherActivity.gDriveSignInIntent(context.actual)
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