package com.anod.appwatcher.backup

import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.backup.gdrive.GDriveUpload
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun createBackupModule(): Module = module {
    factoryOf(::DbBackupManager)
    factoryOf(::ExportBackupTask)
    factoryOf(::ImportBackupTask)
    factoryOf(::GDriveUpload)
    factoryOf(::GDriveSync)
}