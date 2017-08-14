package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.view.MenuItem
import android.view.View
import com.anod.appwatcher.R
import com.anod.appwatcher.fragments.AppWatcherListFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.ui.AppWatcherBaseActivity

/**
 * @author algavris
 * *
 * @date 18/03/2017.
 */

class AppsTagActivity : AppWatcherBaseActivity() {
    private lateinit var mTag: Tag

    override val contentLayout: Int
        get() = R.layout.activity_main
    override val menuResource: Int
        get() = R.menu.main_tag

    override fun onCreate(savedInstanceState: Bundle?) {
        mTag = intentExtras.getParcelable<Tag>(EXTRA_TAG)
        super.onCreate(savedInstanceState)

        val appBarLayout = findViewById<View>(R.id.appbar) as AppBarLayout
        appBarLayout.setBackgroundColor(mTag.color)

        val hsv = FloatArray(3)
        Color.colorToHSV(mTag.color, hsv)
        hsv[2] *= 0.6f
        val dark = Color.HSVToColor(hsv)
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setSelectedTabIndicatorColor(dark)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = dark
        }

        title = mTag.name
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_act_addtag) {
            startActivity(AppsTagSelectActivity.createIntent(mTag, this))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createViewPagerAdapter(): AppWatcherBaseActivity.Adapter {
        val adapter = AppWatcherBaseActivity.Adapter(supportFragmentManager)
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                AppWatcherListFragment.DefaultSection(),
                mTag), getString(R.string.tab_all))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_INSTALLED,
                prefs.sortIndex,
                AppWatcherListFragment.DefaultSection(),
                mTag), getString(R.string.tab_installed))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                prefs.sortIndex,
                AppWatcherListFragment.DefaultSection(),
                mTag), getString(R.string.tab_not_installed))
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
