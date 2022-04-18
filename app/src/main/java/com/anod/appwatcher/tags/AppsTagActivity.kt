package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.WatchListActivity
import com.google.android.material.appbar.MaterialToolbar
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.addMultiWindowFlags
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * *
 * @date 18/03/2017.
 */

class AppsTagActivity : WatchListActivity(), KoinComponent {
    private var tag: Tag = Tag("")

    override val menuResource: Int
        get() = R.menu.tagslist

    override val themeRes: Int
        get() = if (themeColors.statusBarColor.isLight)
            theme.themeLightActionBar
        else
            theme.themeDarkActionBar

    override val themeColors: CustomThemeColors
        get() = CustomThemeColors(tag.color, theme.colors.navigationBarColor)

    override fun onCreate(savedInstanceState: Bundle?) {
        tag = restoreTag(savedInstanceState)
        super.onCreate(savedInstanceState)

        val toolbar = binding.toolbar as MaterialToolbar
        toolbar.setBackgroundColor(tag.color)
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
        if (item.itemId == R.id.menu_act_edittag) {
            EditTagDialog.show(supportFragmentManager, tag, theme)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createViewPagerAdapter(): Adapter {
        val factories = listOf(
                AppsTagListFragment.Factory(Filters.TAB_ALL, prefs.sortIndex, tag, title = getString(R.string.tab_all)),
                AppsTagListFragment.Factory(Filters.INSTALLED, prefs.sortIndex, tag, title = getString(R.string.tab_installed)),
                AppsTagListFragment.Factory(Filters.UNINSTALLED, prefs.sortIndex, tag, title = getString(R.string.tab_not_installed)),
                AppsTagListFragment.Factory(Filters.UPDATABLE, prefs.sortIndex, tag, title = getString(R.string.tab_updatable))
        )
        return Adapter(factories, this)
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"

        fun createTagIntent(tag: Tag, context: Context) = Intent(context, AppsTagActivity::class.java).apply {
            putExtra(EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}