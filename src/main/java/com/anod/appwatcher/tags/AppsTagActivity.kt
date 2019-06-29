package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.watchlist.WatchListActivity
import com.anod.appwatcher.watchlist.WatchListFragment
import info.anodsplace.framework.app.CustomThemeColors
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Alex Gavrishev
 * *
 * @date 18/03/2017.
 */

class AppsTagActivity : WatchListActivity() {
    private var tag: Tag = Tag("")

    override val layoutResource: Int
        get() = R.layout.activity_main
    override val menuResource: Int
        get() = R.menu.tagslist
    override val themeRes: Int
        get() = if (themeColors.statusBarColor.isLight)
            Theme(this).themeLightActionBar
        else
            Theme(this).themeDarkActionBar

    override val themeColors: CustomThemeColors
        get() = CustomThemeColors(tag.color, Theme(this).colors.navigationBarColor)

    override fun onCreate(savedInstanceState: Bundle?) {
        tag = restoreTag(savedInstanceState)
        super.onCreate(savedInstanceState)

        toolbar.setBackgroundColor(tag.color)
        title = tag.name
    }

    private fun restoreTag(savedInstanceState: Bundle?): Tag {
        if (intentExtras.containsKey(EXTRA_TAG)){
            return intentExtras.getParcelable(EXTRA_TAG) ?: Tag("")
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

    override fun createViewPagerAdapter(): Adapter {
        val adapter = Adapter(supportFragmentManager)
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_all))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.INSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_installed))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.UNINSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_not_installed))
        adapter.addFragment(AppsTagListFragment.newInstance(
                Filters.UPDATABLE,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(),
                tag), getString(R.string.tab_updatable))
        return adapter
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"

        fun createTagIntent(tag: Tag, context: Context) = Intent(context, AppsTagActivity::class.java).apply {
            putExtra(EXTRA_TAG, tag)
        }
    }
}
