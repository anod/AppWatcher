package com.anod.appwatcher.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toFile
import com.anod.appwatcher.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImportBackupTask(private val dbBackupManager: DbBackupManager) {

    suspend fun execute(srcUri: Uri): Int = withContext(Dispatchers.IO) {
        if (srcUri.scheme == ContentResolver.SCHEME_FILE) {
            val res = validateFileDestination(srcUri)
            if (res != DbBackupManager.RESULT_OK) {
                return@withContext res
            }
        }
        return@withContext dbBackupManager.doImport(srcUri)
    }

    private fun validateFileDestination(destUri: Uri): Int {
        if (!checkMediaReadable()) {
            return DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE
        }

        val dataFile = destUri.toFile()
        if (!dataFile.exists()) {
            return DbBackupManager.ERROR_FILE_NOT_EXIST
        }
        if (!dataFile.canRead()) {
            return DbBackupManager.ERROR_FILE_READ
        }
        return DbBackupManager.RESULT_OK
    }

    /**
     * Checks if it possible to read from the backup directory

     * @return true/false
     */
    private fun checkMediaReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state && Environment.MEDIA_MOUNTED_READ_ONLY != state) {
            return false
        }
        return true
    }

    companion object {

        fun showImportFinishToast(context: Context, code: Int) {
            when (code) {
                DbBackupManager.RESULT_OK -> Toast.makeText(context, context.getString(R.string.import_done), Toast.LENGTH_SHORT).show()
                DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE -> Toast.makeText(context, context.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show()
                DbBackupManager.ERROR_DESERIALIZE -> Toast.makeText(context, context.getString(R.string.restore_deserialize_failed), Toast.LENGTH_SHORT).show()
                DbBackupManager.ERROR_FILE_READ -> Toast.makeText(context, context.getString(R.string.failed_to_read_file), Toast.LENGTH_SHORT).show()
                DbBackupManager.ERROR_FILE_NOT_EXIST -> Toast.makeText(context, context.getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show()
            }
        }
    }

}