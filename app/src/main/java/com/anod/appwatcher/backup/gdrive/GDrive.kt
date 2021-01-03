package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author alex
 * *
 * @date 1/19/14
 */
class GDrive(private val context: ApplicationContext, private val googleAccount: GoogleSignInAccount, private var listener: Listener? = null) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    constructor(context: Context, googleAccount: GoogleSignInAccount, listener: Listener? = null)
            : this(ApplicationContext(context), googleAccount, listener)

    interface Listener {
        fun onGDriveSyncProgress()
        fun onGDriveSyncStart()
        fun onGDriveSyncFinish()
        fun onGDriveError(exception: UserRecoverableAuthException?)
    }

    fun sync() {
        coroutineScope.launch {
            listener?.onGDriveSyncStart()
            val worker = GDriveSync(context, googleAccount)
            try {
                worker.doSync()
                listener?.onGDriveSyncFinish()
            } catch (e: Exception) {
                AppLog.e("sync exception: ${e.javaClass}", e)
                listener?.onGDriveError(DriveService.extractUserRecoverableException(e))
            }
        }
    }
}