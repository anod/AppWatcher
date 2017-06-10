package com.anod.appwatcher.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment

import java.io.File

class ExportTask(private val mContext: Context, private val mListener: ExportTask.Listener) : AsyncTask<Uri, Void, Int>() {

    interface Listener {
        fun onExportStart()
        fun onExportFinish(code: Int)
    }

    override fun onPreExecute() {
        mListener.onExportStart()
    }

    override fun doInBackground(vararg dest: Uri): Int? {
        val destUri = dest[0]
        val mBackupManager = DbBackupManager(mContext)
        if (destUri.scheme == ContentResolver.SCHEME_FILE) {
            val res = validateFileDestination(destUri)
            if (res != DbBackupManager.RESULT_OK) {
                return res
            }
        }
        return mBackupManager.doExport(destUri)
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
        mListener.onExportFinish(result!!)
    }

}
