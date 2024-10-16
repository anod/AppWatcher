package com.anod.appwatcher

import android.app.Activity
import android.content.Intent
import android.net.UrlQuerySanitizer
import android.os.Bundle
import com.anod.appwatcher.search.SearchComposeActivity

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
                searchIntent.putExtra(SearchComposeActivity.EXTRA_PACKAGE, true)
                searchIntent.putExtra(SearchComposeActivity.EXTRA_KEYWORD, id)
                searchIntent.putExtra(SearchComposeActivity.EXTRA_EXACT, true)
                fallback = false
            }
        }

        if (fallback) {
            val title = intent.getStringExtra(Intent.EXTRA_TITLE)
            if (title != null && title != "") {
                searchIntent.putExtra(SearchComposeActivity.EXTRA_KEYWORD, title)
            } else if (text != null && text != "") {
                searchIntent.putExtra(SearchComposeActivity.EXTRA_KEYWORD, text)
            } else {
                searchIntent.putExtra(SearchComposeActivity.EXTRA_KEYWORD, "")
            }
            searchIntent.putExtra(SearchComposeActivity.EXTRA_EXACT, false)
        }
        searchIntent.putExtra(SearchComposeActivity.EXTRA_SHARE, true)
        startActivity(searchIntent)
        finish()
    }

    companion object {
        private const val URL_PLAYSTORE = "https://play.google.com"
    }
}