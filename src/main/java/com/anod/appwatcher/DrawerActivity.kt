package com.anod.appwatcher

import android.accounts.Account
import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.TagsContentProviderClient
import com.anod.appwatcher.installed.ImportInstalledActivity
import com.anod.appwatcher.tags.AppsTagActivity
import com.anod.appwatcher.utils.Hash
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.wishlist.WishlistFragment
import com.crashlytics.android.Crashlytics
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.framework.app.ToolbarActivity

/**
 * @author algavris
 * @date 01/12/2017
 */
open class DrawerActivity: ToolbarActivity(), AccountSelectionDialog.SelectionListener  {

    override val themeRes: Int
        get() = Theme(this).theme

    private var authToken: String? = null
    private var accountNameView: TextView? = null
    val drawerLayout: DrawerLayout? by lazy { findViewById<DrawerLayout?>(R.id.drawer_layout) }
    val navigationView: NavigationView? by lazy { findViewById<NavigationView?>(R.id.nav_view) }

    open val isHomeAsMenu: Boolean
        get() = false

    val provide: ObjectGraph
        get() = App.provide(this)

    val isAuthenticated: Boolean
        get() = authToken != null

    fun showAccountsDialogWithCheck() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        accountSelectionDialog.show()
    }

    private val accountSelectionDialog: AccountSelectionDialog by lazy {
        AccountSelectionDialog(this, App.provide(this).prefs, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val account = App.provide(this).prefs.account
        if (account== null) {
            accountSelectionDialog.show()
        } else {
            onAccountSelected(account)
        }
    }

    fun setupDrawer() {
        setupToolbar()
        if (this.navigationView != null) {
            this.drawerLayout?.addDrawerListener(object: DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                    drawerView.postDelayed({
                        updateTags()
                    }, 300L)
                }
            })

            setupDrawerContent()
            updateTags()
            val observer = TagsUpdateObserver(this)
            contentResolver.registerContentObserver(DbContentProvider.tagsUri, true, observer)
            contentResolver.registerContentObserver(DbContentProvider.appsTagUri, true, observer)
        }
    }

    private fun setupDrawerContent() {
        val headerView = this.navigationView?.getHeaderView(0) ?: return

        accountNameView = headerView.findViewById<View>(R.id.account_name) as TextView
        val changeAccount = headerView.findViewById<View>(R.id.account_change) as LinearLayout
        changeAccount.setOnClickListener {
            this.accountSelectionDialog.show()
        }

        this.refreshLastUpdateTime()

        this.navigationView?.setNavigationItemSelectedListener { menuItem ->
            this.drawerLayout?.closeDrawers()
            onOptionsItemSelected(menuItem)
            true
        }
    }

    fun refreshLastUpdateTime() {
        val headerView = this.navigationView?.getHeaderView(0) ?: return

        val time = App.provide(this).prefs.lastUpdateTime
        val lastUpdateView = headerView.findViewById<View>(R.id.last_update) as TextView
        if (time > 0) {
            val lastUpdate = getString(R.string.last_update, DateUtils.getRelativeDateTimeString(this, time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0))
            lastUpdateView.text = lastUpdate
            lastUpdateView.visibility = View.VISIBLE
        } else {
            lastUpdateView.visibility = View.GONE
        }
    }

    protected fun setDrawerAccount(account: Account?) {
        if (account == null) {
            accountNameView?.setText(R.string.choose_an_account)
        } else {
            accountNameView?.text = account.name
        }
    }

    fun updateTagsIfVisible() {
        if (this.drawerLayout?.isDrawerVisible(GravityCompat.START) == true) {
            updateTags()
        }
    }

    private fun updateTags() {
        val menu = this.navigationView?.menu ?: return
        menu.removeGroup(1)

        val tagsClient = TagsContentProviderClient(this)
        val counts = tagsClient.queryTagsAppsCounts()
        val cr = tagsClient.queryAll()
        val tags = cr.toList()
        cr.close()
        tagsClient.close()

        tags.forEach { tag ->
            val count = counts.get(tag.id)
            val item = menu.add(1, tag.id, tag.id, tag.name)
            item.setActionView(R.layout.drawer_tag_indicator)
            val tagIndicator = item.actionView.findViewById<View>(android.R.id.text1) as TextView
            val d = ResourcesCompat.getDrawable(resources, R.drawable.circular_color, null)
            DrawableCompat.setTint(DrawableCompat.wrap(d!!), tag.color)
            tagIndicator.setBackgroundDrawable(d)
            tagIndicator.text = if (count > 99) "99+" else "" + count
            item.intent = AppsTagActivity.createTagIntent(tag, this)
        }
        provide.memoryCache.put("tags", tags)
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
                startActivity(Intent(this, ImportInstalledActivity::class.java))
                return true
            }
            R.id.menu_wishlist -> {
                val args = Bundle()
                args.putParcelable(WishlistFragment.EXTRA_ACCOUNT, App.provide(this).prefs.account)
                args.putString(WishlistFragment.EXTRA_AUTH_TOKEN, authToken)
                startActivity(FragmentToolbarActivity.intent(
                        WishlistFragment.TAG,
                        { WishlistFragment() },
                        themeRes,
                        args,
                        this))
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
        AuthTokenAsync(this).request(this, account, object : AuthTokenAsync.Callback {
            override fun onToken(token: String) {
                authToken = token
                Crashlytics.setUserIdentifier(Hash.sha256(account.name).encoded)
                setDrawerAccount(account)
            }

            override fun onError(errorMessage: String) {
                if (App.provide(this@DrawerActivity).networkConnection.isNetworkAvailable) {
                    Toast.makeText(this@DrawerActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DrawerActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                }
                return
            }
        })
    }

    override fun onAccountNotFound(errorMessage: String) {
        if (App.provide(this@DrawerActivity).networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
    }

    internal class TagsUpdateObserver(private val activity: DrawerActivity) : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            activity.updateTagsIfVisible()
        }
    }
}