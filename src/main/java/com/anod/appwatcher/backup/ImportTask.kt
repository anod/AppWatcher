package com.anod.appwatcher.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast

import com.anod.appwatcher.R
import info.anodsplace.appwatcher.framework.ApplicationContext

import java.io.File

class ImportTask(private val context: ApplicationContext, private val listener: ImportTask.Listener) : AsyncTask<Uri, Void, Int>() {

    constructor(context: Context, listener: Listener): this(ApplicationContext(context), listener)

    interface Listener {
        fun onImportFinish(code: Int)
    }

    override fun doInBackground(vararg sources: Uri): Int? {
        val srcUri = sources[0]

        if (srcUri.scheme == ContentResolver.SCHEME_FILE) {
            val res = validateFileDestination(srcUri)
            if (res != DbBackupManager.RESULT_OK) {
                return res
            }
        }
        return DbBackupManager(context.actual).doImport(srcUri)
    }

    private fun validateFileDestination(destUri: Uri): Int {
        if (!checkMediaReadable()) {
            return DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE
        }

        val dataFile = File(destUri.path)
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

    override fun onPostExecute(result: Int?) {
        listener.onImportFinish(result!!)
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
