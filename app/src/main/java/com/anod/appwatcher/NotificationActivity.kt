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
import info.anodsplace.framework.app.addMultiWindowFlags
import info.anodsplace.framework.content.startActivitySafely
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NotificationActivity : Activity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sn = SyncNotification(info.anodsplace.context.ApplicationContext(this), get())
        sn.cancel()
        when (intent.getIntExtra(EXTRA_ACTION_TYPE, 0)) {
            ACTION_PLAY_STORE -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: ""
                startActivitySafely(Intent().forPlayStore(pkg).addMultiWindowFlags(this))
            }

            ACTION_MY_APPS -> startActivitySafely(Intent().forMyApps(false).addMultiWindowFlags(this))
            ACTION_MARK_VIEWED -> {
                prefs.isLastUpdatesViewed = true
            }
        }
        finish()
    }

    companion object {
        private const val EXTRA_ACTION_TYPE = "type"
        const val EXTRA_PACKAGE = "pkg"

        const val ACTION_PLAY_STORE = 1
        const val ACTION_DISMISS = 2
        const val ACTION_MY_APPS = 3
        const val ACTION_MARK_VIEWED = 4

        fun intent(uri: Uri, type: Int, context: Context) = Intent(context, NotificationActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = uri
                putExtra(EXTRA_ACTION_TYPE, type)
            }
    }
}