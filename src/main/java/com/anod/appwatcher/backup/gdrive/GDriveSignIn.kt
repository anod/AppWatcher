package com.anod.appwatcher.backup.gdrive

import android.app.Activity
import com.google.android.gms.tasks.Task
import android.content.Intent
import com.anod.appwatcher.framework.ActivityListener
import com.anod.appwatcher.framework.GoogleSignInConnect
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import info.anodsplace.android.log.AppLog
import com.google.android.gms.auth.api.signin.GoogleSignInAccount





/**
 * @author algavris
 * @date 28/11/2017
 */
class GDriveSignIn(private val activity: Activity, private val listener: Listener): ActivityListener.ResultListener {

    private val driveConnect by lazy { GoogleSignInConnect(activity, createGDriveSignInOptions()) }

    companion object {
        const val codeGDriveSignIn = 123
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
                activity.startActivityForResult(client.signInIntent, codeGDriveSignIn)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == codeGDriveSignIn) {
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