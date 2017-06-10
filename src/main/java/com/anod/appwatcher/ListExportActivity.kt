package com.anod.appwatcher

import android.os.Bundle
import android.support.v4.app.FragmentActivity

import com.anod.appwatcher.backup.DbBackupManager

class ListExportActivity : FragmentActivity() {
    lateinit var backupManager: DbBackupManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_import)

        backupManager = DbBackupManager(this)
    }
}
