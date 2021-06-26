package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author alex
 * *
 * @date 1/19/14
 */
class GDrive(private val context: ApplicationContext, private val googleAccount: GoogleSignInAccount) {
    constructor(context: Context, googleAccount: GoogleSignInAccount)
            : this(ApplicationContext(context), googleAccount)

    class SyncError(val error: UserRecoverableAuthException?) : Exception()

    suspend fun sync() {
        val worker = GDriveSync(context, googleAccount)
        try {
            worker.doSync()
        } catch (e: Exception) {
            AppLog.e("sync exception: ${e.javaClass}", e)
            throw SyncError(DriveService.extractUserRecoverableException(e))
        }
    }
}