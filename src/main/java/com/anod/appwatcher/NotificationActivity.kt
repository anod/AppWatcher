package com.anod.appwatcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.framework.forMyApps
import com.anod.appwatcher.framework.forPlayStore
import com.anod.appwatcher.framework.startActivitySafely

class NotificationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sn = SyncNotification(this)
        sn.cancel()
        val type = intent.getIntExtra(EXTRA_TYPE, 0)
        when (type) {
            TYPE_PLAY -> {
                val pkg = intent.getStringExtra(EXTRA_PKG)
                this.startActivitySafely(Intent().forPlayStore(pkg))
            }
            TYPE_MYAPPS -> this.startActivitySafely(Intent().forMyApps(false))
            TYPE_MYAPPS_UPDATE -> this.startActivitySafely(Intent().forMyApps(true))
            // TYPE_DISMISS -> nothing
        }
        finish()
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_PKG = "pkg"

        const val TYPE_PLAY = 1
        const val TYPE_DISMISS = 2
        const val TYPE_MYAPPS = 3
        const val TYPE_MYAPPS_UPDATE = 4
    }
}
