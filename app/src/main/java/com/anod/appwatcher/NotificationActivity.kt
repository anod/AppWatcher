package com.anod.appwatcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.utils.forMyApps
import com.anod.appwatcher.utils.forPlayStore
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.startActivitySafely

class NotificationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sn = SyncNotification(ApplicationContext(this))
        sn.cancel()
        when (intent.getIntExtra(EXTRA_TYPE, 0)) {
            TYPE_PLAY -> {
                val pkg = intent.getStringExtra(EXTRA_PKG) ?: ""
                startActivitySafely(Intent().forPlayStore(pkg))
            }
            TYPE_MYAPPS -> startActivitySafely(Intent().forMyApps(false))
        }
        finish()
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_PKG = "pkg"

        const val TYPE_PLAY = 1
        const val TYPE_DISMISS = 2
        const val TYPE_MYAPPS = 3
    }
}
