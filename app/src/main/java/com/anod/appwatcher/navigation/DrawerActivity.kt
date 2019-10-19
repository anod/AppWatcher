package com.anod.appwatcher.navigation

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.anod.appwatcher.*
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.ImportInstalledFragment
import com.anod.appwatcher.tags.AppsTagActivity
import com.anod.appwatcher.utils.Hash
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.wishlist.WishlistFragment
import com.crashlytics.android.Crashlytics
import com.google.android.material.navigation.NavigationView
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ToolbarActivity

/**
 * @author Alex Gavrishev
 * @date 01/12/2017
 */
abstract class DrawerActivity: ToolbarActivity(), AccountSelectionDialog.SelectionListener  {

    override val themeRes: Int
        get() = Theme(this).theme

    private var authToken: String? = null
    private val drawerLayout: DrawerLayout? by lazy { findViewById<DrawerLayout?>(R.id.drawer_layout) }
    private val navigationView: NavigationView? by lazy { findViewById<NavigationView?>(R.id.nav_view) }
    private val accountNameView: TextView? by lazy { navigationView?.getHeaderView(0)?.findViewById<TextView>(R.id.account_name) }
    private val drawerViewModel: DrawerViewModel by viewModels()

    open val isHomeAsMenu: Boolean
        get() = false

    val provide: AppComponent
        get() = Application.provide(this)

    fun showAccountsDialogWithCheck() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        accountSelectionDialog.show()
    }

    private val accountSelectionDialog: AccountSelectionDialog by lazy {
        AccountSelectionDialog(this, Application.provide(this).prefs, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDrawer()

        val account = Application.provide(this).prefs.account
        if (account== null) {
            accountSelectionDialog.show()
        } else {
            onAccountSelected(account)
        }
    }

    private fun setupDrawer() {
        this.navigationView ?: return

        setupHeader(drawerViewModel)
        drawerViewModel.refreshLastUpdateTime()
    }

    private fun setupHeader(viewModel: DrawerViewModel) {
        val headerView = navigationView?.getHeaderView(0) ?: return

        val changeAccount = headerView.findViewById<View>(R.id.account_change) as LinearLayout
        changeAccount.setOnClickListener {
            this.accountSelectionDialog.show()
        }

        viewModel.account.observe(this, Observer {
            updateDrawerAccount(it)
        })

        viewModel.lastUpdateTime.observe(this, Observer {
            updateLastUpdateTime(it ?: 0)
        })

        viewModel.tags.observe(this, Observer {
            updateTags(it ?: emptyList())
        })

        this.navigationView?.setNavigationItemSelectedListener { menuItem ->
            this.drawerLayout?.closeDrawers()
            onOptionsItemSelected(menuItem)
            true
        }
    }

    private fun updateLastUpdateTime(time: Long) {
        val headerView = this.navigationView?.getHeaderView(0) ?: return
        val lastUpdateView = headerView.findViewById<View>(R.id.last_update) as TextView
        if (time > 0) {
            val lastUpdate = getString(R.string.last_update, DateUtils.getRelativeDateTimeString(this, time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0))
            lastUpdateView.text = lastUpdate
            lastUpdateView.visibility = View.VISIBLE
        } else {
            lastUpdateView.visibility = View.GONE
        }
    }

    private fun updateDrawerAccount(account: Account?) {
        if (account == null) {
            accountNameView?.setText(R.string.choose_an_account)
        } else {
            accountNameView?.text = account.name
        }
    }

    private fun updateTags(result: List<Pair<Tag, Int>>) {
        val menu = this.navigationView?.menu ?: return
        menu.removeGroup(1)
        result.forEach { (tag, count) ->
            val item = menu.add(1, tag.id, Menu.NONE, tag.name)
            item.setActionView(R.layout.drawer_tag_indicator)
            val tagIndicator = item.actionView.findViewById<View>(android.R.id.text1) as TextView
            val d = ResourcesCompat.getDrawable(resources, R.drawable.circular_color, null)
            DrawableCompat.setTint(DrawableCompat.wrap(d!!), tag.color)
            tagIndicator.setBackgroundDrawable(d)
            tagIndicator.text = if (count > 99) "99+" else "" + count
            item.intent = AppsTagActivity.createTagIntent(tag, this@DrawerActivity)
        }
        provide.memoryCache.put("tags", result.map { it.first })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (this.isHomeAsMenu) {
                    this.drawerLayout?.openDrawer(GravityCompat.START)
                    return true
                }
                return super.onOptionsItemSelected(item)
            }
            R.id.menu_add -> {
                val addActivity = Intent(this, MarketSearchActivity::class.java)
                startActivity(addActivity)
                return true
            }
            R.id.menu_settings -> {
                val settingsActivity = Intent(this, SettingsActivity::class.java)
                startActivity(settingsActivity)
                return true
            }
            R.id.menu_act_import -> {
                startActivity(ImportInstalledFragment.intent(
                        this,
                        themeRes,
                        themeColors))
                return true
            }
            R.id.menu_wishlist -> {
                val account = Application.provide(this).prefs.account
                startActivity(WishlistFragment.intent(
                        this,
                        themeRes, themeColors,
                        account, authToken))
                return true
            }
        }
        val intent = item.intent
        if (intent != null) {
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        accountSelectionDialog.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        accountSelectionDialog.onActivityResult(requestCode, resultCode, data)
    }

    override fun onAccountSelected(account: Account) {
        drawerViewModel.account.value = account
        val collectReports = provide.prefs.collectCrashReports
        AuthTokenAsync(this).request(this, account) { token ->
            if (token.isNotBlank()) {
                onAuthToken(token)
                if (collectReports) {
                    Crashlytics.setUserIdentifier(Hash.sha256(account.name).encoded)
                }
                updateDrawerAccount(account)
            } else {
                AppLog.e("Error retrieving authentication token")
                onAuthToken("")
                if (Application.provide(this@DrawerActivity).networkConnection.isNetworkAvailable) {
                    Toast.makeText(this@DrawerActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DrawerActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    open fun onAuthToken(authToken: String) {
        this.authToken = authToken
    }

    override fun onAccountNotFound(errorMessage: String) {
        if (Application.provide(this@DrawerActivity).networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
    }

}