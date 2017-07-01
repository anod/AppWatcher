package com.anod.appwatcher

import android.app.Activity
import android.os.Bundle
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.utils.IntentUtils

class NotificationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val sn = SyncNotification(this)
        sn.cancel()
        val type = intent.getIntExtra(EXTRA_TYPE, 0)
        if (type == TYPE_PLAY) {
            val pkg = intent.getStringExtra(EXTRA_PKG)
            IntentUtils.startActivitySafely(this, IntentUtils.createPlayStoreIntent(pkg))
        } else if (type == TYPE_MYAPPS) {
            IntentUtils.startActivitySafely(this, IntentUtils.createMyAppsIntent(false))
        } else if (type == TYPE_MYAPPS_UPDATE) {
            IntentUtils.startActivitySafely(this, IntentUtils.createMyAppsIntent(true))
        }/* if (type == TYPE_DISMISS) {
            // Nothing
        } */

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
