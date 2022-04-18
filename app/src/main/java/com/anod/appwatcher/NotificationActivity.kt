package com.anod.appwatcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.utils.forMyApps
import com.anod.appwatcher.utils.forPlayStore
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.startActivitySafely
import org.koin.core.component.KoinComponent

class NotificationActivity : Activity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sn = SyncNotification(ApplicationContext(this))
        sn.cancel()
        when (intent.getIntExtra(extraActionType, 0)) {
            actionPlayStore -> {
                val pkg = intent.getStringExtra(extraPackage) ?: ""
                startActivitySafely(Intent().forPlayStore(pkg, this))
            }
            actionMyApps -> startActivitySafely(Intent().forMyApps(false, this))
            actionMarkViewed -> {
                prefs.isLastUpdatesViewed = true
            }
        }
        finish()
    }

    companion object {
        private const val extraActionType = "type"
        const val extraPackage = "pkg"

        const val actionPlayStore = 1
        const val actionDismiss = 2
        const val actionMyApps = 3
        const val actionMarkViewed = 4

        fun intent(uri: Uri, type: Int, context: Context) = Intent(context, NotificationActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    data = uri
                    putExtra(extraActionType, type)
                }
    }
}