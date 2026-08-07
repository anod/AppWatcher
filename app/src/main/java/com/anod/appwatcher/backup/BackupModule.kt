package com.anod.appwatcher.backup

import android.app.backup.BackupAgentHelper
import android.content.Context
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.backup.gdrive.GDriveUpload
import com.anod.appwatcher.preferences.Preferences
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

class AppWatcherBackupAgent : BackupAgentHelper() {
    override fun onRestoreFinished() {
        super.onRestoreFinished()
        check(
            getSharedPreferences(
                Preferences.DEVICE_PREFS_NAME,
                Context.MODE_PRIVATE
            ).edit()
                .putBoolean(Preferences.RESTORED_FROM_BACKUP, true)
                .commit()
        ) {
            "Unable to persist restore state"
        }
    }
}

fun createBackupModule(): Module = module {
    factoryOf(::DbBackupManager)
    factoryOf(::ExportBackupTask)
    factoryOf(::ImportBackupTask)
    factoryOf(::GDriveUpload)
    factoryOf(::GDriveSync)
}