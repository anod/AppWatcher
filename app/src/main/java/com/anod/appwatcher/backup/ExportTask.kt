package com.anod.appwatcher.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toFile
import com.anod.appwatcher.R
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExportTask(private val context: ApplicationContext) {

    companion object {
        fun showFinishToast(ctx: Context, result: Int) {
            when (result) {
                DbBackupManager.RESULT_OK -> Toast.makeText(
                        ctx,
                        R.string.export_done,
                        Toast.LENGTH_SHORT
                ).show()
                DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE -> Toast.makeText(
                        ctx,
                        R.string.external_storage_not_available,
                        Toast.LENGTH_SHORT
                ).show()
                DbBackupManager.ERROR_FILE_WRITE -> Toast.makeText(
                        ctx,
                        R.string.failed_to_write_file,
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

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

        val destFile = destUri.toFile()
        if (destFile.parentFile?.exists() == false) {
            destFile.parentFile!!.mkdirs()
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
