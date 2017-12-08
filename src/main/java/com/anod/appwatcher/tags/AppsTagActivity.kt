package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.MenuItem
import android.view.View
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.WatchListFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.watchlist.WatchListActivity
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * *
 * @date 18/03/2017.
 */

class AppsTagActivity : WatchListActivity() {
    private var tag: Tag = Tag("")

    override val contentLayout: Int
        get() = R.layout.activity_main
    override val menuResource: Int
        get() = R.menu.tagslist

    override fun onCreate(savedInstanceState: Bundle?) {
        tag = restoreTag(savedInstanceState)
        super.onCreate(savedInstanceState)

        if (tag.id == 0) {
            AppLog.e("Tag is empty")
            startActivity(Intent(this, AppWatcherActivity::class.java))
            finish()
            return
        }

        val appBarLayout = findViewById<View>(R.id.appbar) as AppBarLayout
        appBarLayout.setBackgroundColor(tag.color)

        val hsv = FloatArray(3)
        Color.colorToHSV(tag.color, hsv)
        hsv[2] *= 0.6f
        val dark = Color.HSVToColor(hsv)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = dark
        }

        title = tag.name
    }

    fun restoreTag(savedInstanceState: Bundle?): Tag {
        if (intentExtras.containsKey(EXTRA_TAG)){
            return intentExtras.getParcelable(EXTRA_TAG)
        } else if (savedInstanceState != null){
            val savedTag = savedInstanceState.getParcelable<Tag?>(EXTRA_TAG)
            if (savedTag != null) {
                return savedTag
            }
        }
        return Tag("")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_TAG, tag)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_act_addtag) {
            startActivity(AppsTagSelectActivity.createIntent(tag, this))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createViewPagerAdapter(): WatchListActivity.Adapter {
        val adapter = WatchListActivity.Adapter(supportFragmentManager)
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_all))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_INSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_installed))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_not_installed))
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_UPDATABLE,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), getString(R.string.tab_updatable))
        return adapter
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"

        fun createTagIntent(tag: Tag, context: Context): Intent {
            val intent = Intent(context, AppsTagActivity::class.java)
            intent.putExtra(EXTRA_TAG, tag)
            return intent
        }
    }
}
