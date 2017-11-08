package com.anod.appwatcher

import android.app.Activity
import android.content.Intent
import android.net.UrlQuerySanitizer
import android.os.Bundle
import com.anod.appwatcher.search.SearchActivity

class ShareRecieverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)

        val searchIntent = Intent(this, MarketSearchActivity::class.java)
        var fallback = true
        if (text != null && text.startsWith(URL_PLAYSTORE)) {
            val sanitizer = UrlQuerySanitizer(text)
            val id = sanitizer.getValue("id")
            if (id != null) {
                searchIntent.putExtra(SearchActivity.EXTRA_PACKAGE, true)
                searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, id)
                searchIntent.putExtra(SearchActivity.EXTRA_EXACT, true)
                fallback = false
            }
        }

        if (fallback) {
            val title = intent.getStringExtra(Intent.EXTRA_TITLE)
            if (title != null && title != "") {
                searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, title)
            } else if (text != null && text != "") {
                searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, text)
            } else {
                searchIntent.putExtra(SearchActivity.EXTRA_KEYWORD, "")
            }
            searchIntent.putExtra(SearchActivity.EXTRA_EXACT, false)
        }
        searchIntent.putExtra(SearchActivity.EXTRA_SHARE, true)
        startActivity(searchIntent)
        finish()
    }

    companion object {
        private const val URL_PLAYSTORE = "https://play.google.com"
    }
}
