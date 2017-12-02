package com.anod.appwatcher.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import info.anodsplace.appwatcher.framework.ApplicationContext

import java.io.File

class ExportTask(private val context: ApplicationContext, private val listener: ExportTask.Listener) : AsyncTask<Uri, Void, Int>() {

    constructor(context: Context, listener: Listener): this(ApplicationContext(context), listener)

    interface Listener {
        fun onExportStart()
        fun onExportFinish(code: Int)
    }

    override fun onPreExecute() {
        listener.onExportStart()
    }

    override fun doInBackground(vararg dest: Uri): Int? {
        val destUri = dest[0]

        if (destUri.scheme == ContentResolver.SCHEME_FILE) {
            val res = validateFileDestination(destUri)
            if (res != DbBackupManager.RESULT_OK) {
                return res
            }
        }
        return DbBackupManager(context.actual).doExport(destUri)
    }

    private fun validateFileDestination(destUri: Uri): Int {
        if (!checkMediaWritable()) {
            return DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE
        }

        val destFile = File(destUri.path)
        if (!destFile.parentFile.exists()) {
            destFile.parentFile.mkdirs()
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


    override fun onPostExecute(result: Int?) {
        listener.onExportFinish(result!!)
    }

}
