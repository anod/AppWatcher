package com.anod.appwatcher

import android.os.Bundle
import com.anod.appwatcher.watchlist.MainActivity

class AppWatcherActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)
    }
}