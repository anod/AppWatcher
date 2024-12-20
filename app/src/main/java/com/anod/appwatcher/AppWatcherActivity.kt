package com.anod.appwatcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.anod.appwatcher.watchlist.MainActivity
import com.anod.appwatcher.watchlist.WatchListStateViewModel

class AppWatcherActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun createTagShortcutIntent(tagId: Int, initialColor: Int, context: Context) = Intent(context, AppWatcherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse("com.anod.appwatcher://tags/$tagId?color=$initialColor")
            putExtra(WatchListStateViewModel.EXTRA_TAG_ID, tagId)
            putExtra(WatchListStateViewModel.EXTRA_TAG_COLOR, initialColor)
        }
    }
}