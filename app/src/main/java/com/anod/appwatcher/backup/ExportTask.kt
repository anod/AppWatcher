package com.anod.appwatcher.backup

import android.content.ContentResolver
import android.net.Uri
import android.os.Environment
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File

class ExportTask(private val context: ApplicationContext) {

    suspend fun execute(destUri: Uri): Int = withContext(Dispatchers.IO) {

        if (destUri.scheme == ContentResolver.SCHEME_FILE) {
            val res = validateFileDestination(destUri)
            if (res != DbBackupManager.RESULT_OK) {
                return@withContext res
            }
        }
        return@withContext DbBackupManager(context.actual).doExport(destUri)
    }

    private fun validateFileDestination(destUri: Uri): Int {
        if (!checkMediaWritable()) {
            return DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE
        }

        val destFile = File(destUri.path!!)
        if (destFile.parentFile?.exists() == false) {
            destFile.parentFile?.mkdirs()
        }

        return DbBackupManager.RESULT_OK
    }

    /**
     * Checks if it possible to write to the backup directory

     * @return true/false
     */
    private fun checkMediaWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state) {
            // We can read and write the media
            return false
        }
        return true
    }
}
