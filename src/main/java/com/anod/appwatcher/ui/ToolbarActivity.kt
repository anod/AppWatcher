package com.anod.appwatcher.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.R

/**
 * @author alex
 * *
 * @date 2015-06-20
 */
open class ToolbarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = Preferences(this)
        AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
        super.onCreate(savedInstanceState)
    }

    protected fun setupToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        //set the Toolbar as ActionBar
        setSupportActionBar(toolbar)

        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //if (!getSupportFragmentManager().popBackStackImmediate()) {
                //    NavUtils.navigateUpFromSameTask(this);
                //}
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected val intentExtras: Bundle
        get() {
            if (intent == null || intent.extras == null) {
                return Bundle()
            }
            return intent.extras
        }
}
