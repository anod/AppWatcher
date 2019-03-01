package com.anod.appwatcher

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.CustomThemeColors

class ListExportActivity : FragmentActivity(), CustomThemeActivity {

    override val themeRes: Int
        get() = Theme(this).themeDialog
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    lateinit var backupManager: DbBackupManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_import)

        backupManager = DbBackupManager(this)
    }
}
