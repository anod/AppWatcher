package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.watchlist.WatchListActivity
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.addMultiWindowFlags

/**
 * @author Alex Gavrishev
 * *
 * @date 18/03/2017.
 */

class AppsTagActivity : WatchListActivity() {
    private var tag: Tag = Tag("")

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

        binding.toolbar.setBackgroundColor(tag.color)
        title = tag.name
    }

    private fun restoreTag(savedInstanceState: Bundle?): Tag {
        if (intentExtras.containsKey(EXTRA_TAG)) {
            return intentExtras.getParcelable(EXTRA_TAG) ?: Tag("")
        } else if (savedInstanceState != null) {
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
            AppsTagSelectDialog.show(tag, supportFragmentManager)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createViewPagerAdapter(): Adapter {
        val adapter = Adapter(this)

        adapter.addFragment(AppsTagListFragment.Factory(
                Filters.TAB_ALL,
                prefs.sortIndex,
                tag), getString(R.string.tab_all))
        adapter.addFragment(AppsTagListFragment.Factory(
                Filters.INSTALLED,
                prefs.sortIndex,
                tag), getString(R.string.tab_installed))
        adapter.addFragment(AppsTagListFragment.Factory(
                Filters.UNINSTALLED,
                prefs.sortIndex,
                tag), getString(R.string.tab_not_installed))
        adapter.addFragment(AppsTagListFragment.Factory(
                Filters.UPDATABLE,
                prefs.sortIndex,
                tag), getString(R.string.tab_updatable))
        return adapter
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"

        fun createTagIntent(tag: Tag, context: Context) = Intent(context, AppsTagActivity::class.java).apply {
            putExtra(EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}
