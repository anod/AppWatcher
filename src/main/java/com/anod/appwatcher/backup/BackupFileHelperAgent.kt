package com.anod.appwatcher.backup

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.os.ParcelFileDescriptor

import com.anod.appwatcher.model.DbSchemaManager

import java.io.File
import java.io.IOException

class BackupFileHelperAgent : BackupAgentHelper() {

    override fun onCreate() {
        val dbs = FileBackupHelper(this, DbSchemaManager.dbName)
        addHelper(APPDB_BACKUP_KEY, dbs)
    }

    override fun getFilesDir(): File {
        val path = getDatabasePath(DbSchemaManager.dbName)
        return path.parentFile
    }

    @Throws(IOException::class)
    override fun onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput,
                          newState: ParcelFileDescriptor) {
        // Hold the lock while the FileBackupHelper performs backup
        synchronized(BackupFileHelperAgent.sDataLock) {
            super.onBackup(oldState, data, newState)
        }
    }

    @Throws(IOException::class)
    override fun onRestore(data: BackupDataInput, appVersionCode: Int,
                           newState: ParcelFileDescriptor) {
        // Hold the lock while the FileBackupHelper restores the file
        synchronized(BackupFileHelperAgent.sDataLock) {
            super.onRestore(data, appVersionCode, newState)
        }
    }

    companion object {
        // A key to uniquely identify the set of backup data
        private const val APPDB_BACKUP_KEY = "appdb"
        // Object for intrinsic lock
        private val sDataLock = arrayOfNulls<Any>(0)
    }
}
