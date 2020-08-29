package com.anod.appwatcher

import android.content.Context
import android.content.Intent
import com.anod.appwatcher.search.SearchActivity

class MarketSearchActivity : SearchActivity() {

    companion object {

        fun intent(context: Context, keyword: String, focus: Boolean): Intent = Intent(context, MarketSearchActivity::class.java).apply {
            putExtra(EXTRA_KEYWORD, keyword)
            putExtra(EXTRA_FOCUS, focus)
        }
    }
}