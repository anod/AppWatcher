package com.anod.appwatcher

import android.os.Bundle
import com.anod.appwatcher.watchlist.WatchListActivity

class AppWatcherActivity : WatchListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)
    }
}