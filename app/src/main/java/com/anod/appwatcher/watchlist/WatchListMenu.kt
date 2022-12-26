package com.anod.appwatcher.watchlist

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.installed.InstalledActivity
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.forMyApps
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.view.MenuItemAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class MenuAction
class SortMenuAction(val sortId: Int) : MenuAction()
class FilterMenuAction(val filterId: Int) : MenuAction()
class SearchQueryAction(val query: String, val submit: Boolean) : MenuAction()

class SearchMenu(
        private val action: EventFlow<MenuAction>
) : SearchView.OnQueryTextListener {
    private var menuItem: MenuItem? = null

    var expand = false
    var query = ""
        set(value) {
            this.expand = value.isNotBlank()
            field = value
        }

    fun init(searchMenuItem: MenuItem?, context: Context) {
        menuItem = searchMenuItem ?: return
        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                onQueryTextChange("")
                return true
            }
        })

        val searchView = (menuItem?.actionView as? SearchView)?.apply {
            setOnQueryTextListener(this@SearchMenu)
            isSubmitButtonEnabled = true
            queryHint = context.getString(R.string.search)
        }

        if (expand) {
            menuItem?.expandActionView()
            searchView?.setQuery(query, false)
        }
    }

    private fun collapseSearch() {
        menuItem?.collapseActionView()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        this.query = ""
        if (query.isEmpty()) {
            collapseSearch()
        } else {
            onQueryTextChange("")
            collapseSearch()
        }
        action.tryEmit(SearchQueryAction(query, true))
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        query = newText
        action.tryEmit(SearchQueryAction(newText, false))
        return true
    }
}

/**
 * @author Alex Gavrishev
 * @date 03/12/2017
 */
class WatchListMenu(
        private val action: EventFlow<MenuAction>,
        private val activity: AppCompatActivity,
        private val appScope: CoroutineScope,
        private val prefs: Preferences
) {
    val search = SearchMenu(action)
    var filterId: Int = Filters.ALL
        set(value) {
            updateFilterItem(value)
            field = value
        }

    private val refreshMenuAnimation = MenuItemAnimation(activity, info.anodsplace.framework.R.anim.rotate)

    private var filterItem: MenuItem? = null

    fun init(menu: Menu) {
        search.init(menu.findItem(R.id.menu_act_search), activity)
        refreshMenuAnimation.menuItem = menu.findItem(R.id.menu_act_refresh)
        filterItem = menu.findItem(R.id.menu_act_filter)
        updateFilterItem(filterId)
    }

    private fun updateFilterItem(filterId: Int) {
        if (filterId == Filters.ALL) {
            filterItem?.icon = activity.resources.getDrawable(R.drawable.ic_flash_off_24dp, activity.theme)
        } else {
            filterItem?.icon = activity.resources.getDrawable(R.drawable.ic_flash_on_24dp, activity.theme)
        }
        filterItem?.subMenu?.getItem(filterId)?.isChecked = true
    }

    fun startRefresh() {
        refreshMenuAnimation.start()
    }

    fun stopRefresh() {
        refreshMenuAnimation.stop()
    }

    fun onItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                val addActivity = Intent(activity, MarketSearchActivity::class.java)
                activity.startActivity(addActivity)
                return true
            }
            R.id.menu_act_refresh -> {
                appScope.launch {
                    ViewModelProvider(activity)
                            .get(WatchListStateViewModel::class.java)
                            .requestRefresh()
                            .collect { }
                }
                return true
            }
            R.id.menu_settings -> {
                val settingsActivity = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(settingsActivity)
                return true
            }
            R.id.menu_act_installed -> {
                activity.startActivity(InstalledActivity.intent(
                        false,
                        activity))
                return true
            }
            R.id.menu_act_sort -> {
                val selected = prefs.sortIndex
                DialogSingleChoice(activity, R.style.AlertDialog, R.array.sort_titles, selected) { dialog, index ->
                    prefs.sortIndex = index
                    action.tryEmit(SortMenuAction(index))
                    dialog.dismiss()
                }.show()
                return true
            }
            R.id.menu_filter_all,
            R.id.menu_filter_installed,
            R.id.menu_filter_not_installed,
            R.id.menu_filter_updatable -> {
                action.tryEmit(FilterMenuAction(item.order))
                return true
            }
            R.id.menu_my_apps -> {
                activity.startActivitySafely(Intent().forMyApps(true, activity))
                return true
            }
        }
        return false
    }

}