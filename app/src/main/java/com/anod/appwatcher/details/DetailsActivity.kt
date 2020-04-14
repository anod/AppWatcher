package com.anod.appwatcher.details

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.commitNow
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ToolbarActivity

abstract class DetailsActivity : ToolbarActivity() {

    override val themeRes: Int
        get() = Theme(this).themeDialogNoActionBar

    override val layoutResource: Int
        get() = R.layout.activity_app_changelog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: ""
        val detailsUrl = intent.getStringExtra(EXTRA_DETAILS_URL) ?: ""
        val rowId = intent.getIntExtra(EXTRA_ROW_ID, -1)

        if (appId.isEmpty()) {
            Toast.makeText(this, getString(R.string.cannot_load_app, appId), Toast.LENGTH_LONG).show()
            AppLog.e("Cannot loadChangelog app details: '${appId}'")
            finish()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.content, DetailsFragment.newInstance(appId, detailsUrl, rowId), DetailsFragment.tag)
            }
        }
    }

    companion object {
        const val EXTRA_APP_ID = "app_id"
        const val EXTRA_DETAILS_URL = "url"
        const val EXTRA_ROW_ID = "row_id"
    }
}