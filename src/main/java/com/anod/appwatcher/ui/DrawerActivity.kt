package com.anod.appwatcher.ui

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
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.accounts.AccountChooser
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.TagsContentProviderClient
import com.anod.appwatcher.fragments.AccountChooserFragment
import com.anod.appwatcher.installed.ImportInstalledActivity
import com.anod.appwatcher.tags.AppsTagActivity
import com.anod.appwatcher.wishlist.WishlistFragment

/**
 * @author alex
 * *
 * @date 2014-08-07
 */
abstract class DrawerActivity : ToolbarActivity(), AccountChooser.OnAccountSelectionListener {
    private var mDrawerLayout: DrawerLayout? = null
    private var mAccountNameView: TextView? = null
    private var mNavigationView: NavigationView? = null
    private var mAuthToken: String? = null

    lateinit var mAccountChooser: AccountChooser
    lateinit var mPreferences: Preferences

    protected open val isDrawerEnabled: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = Preferences(this)
        mAccountChooser = AccountChooser(this, mPreferences, this)
        mAccountChooser.init()
    }

    protected fun setupDrawer() {
        setupToolbar()

        if (isDrawerEnabled) {
            mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
            mNavigationView = findViewById(R.id.nav_view) as NavigationView
            setupDrawerContent(mNavigationView!!)
            updateTags()
            val observer = TagsUpdateObserver(this)
            contentResolver.registerContentObserver(DbContentProvider.TAGS_CONTENT_URI, true, observer)
            contentResolver.registerContentObserver(DbContentProvider.APPS_TAG_CONTENT_URI, true, observer)
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        val headerView = navigationView.getHeaderView(0)

        mAccountNameView = headerView.findViewById(R.id.account_name) as TextView
        val changeAccount = headerView.findViewById(R.id.account_change) as LinearLayout
        changeAccount.setOnClickListener { onAccountChooseClick() }

        val pref = Preferences(this)
        val time = pref.lastUpdateTime

        val lastUpdateView = headerView.findViewById(R.id.last_update) as TextView
        if (time > 0) {
            val lastUpdate = getString(R.string.last_update, DateUtils.getRelativeDateTimeString(this, time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0))
            lastUpdateView.text = lastUpdate
            lastUpdateView.visibility = View.VISIBLE
        } else {
            lastUpdateView.visibility = View.GONE/**/
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            mDrawerLayout?.closeDrawers()
            onOptionsItemSelected(menuItem)
            true
        }

    }

    fun updateTags() {
        val menu = mNavigationView!!.menu
        menu.removeGroup(1)

        val tags = TagsContentProviderClient(this)
        val counts = tags.queryTagsAppsCounts()

        val cr = tags.queryAll()
        cr.moveToPosition(-1)
        while (cr.moveToNext()) {
            val tag = cr.tag
            val count = counts.get(tag.id)
            val item = menu.add(1, tag.id, tag.id, tag.name)
            item.setActionView(R.layout.drawer_tag_indicator)
            val tagIndicator = item.actionView.findViewById(android.R.id.text1) as TextView
            val d = ResourcesCompat.getDrawable(resources, R.drawable.circular_color, null)
            DrawableCompat.setTint(DrawableCompat.wrap(d!!), tag.color)
            tagIndicator.setBackgroundDrawable(d)
            tagIndicator.text = if (count > 100) "99+" else "" + count
            item.intent = AppsTagActivity.createTagIntent(tag, this)
        }
        cr.close()
    }

    override val accountSelectionListener: AccountChooserFragment.OnAccountSelectionListener
        get() = mAccountChooser

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mAccountChooser.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    protected fun onAccountChooseClick() {
        mAccountChooser.showAccountsDialogWithCheck()
    }

    override fun onAccountSelected(account: Account, authSubToken: String?) {
        mAuthToken = authSubToken
        if (authSubToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            return
        }

        if (isDrawerEnabled) {
            setDrawerAccount(account)
        }
    }

    override fun onAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isDrawerEnabled) {
                    mDrawerLayout?.openDrawer(GravityCompat.START)
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
                args.putParcelable(WishlistFragment.EXTRA_ACCOUNT, mAccountChooser.account)
                args.putString(WishlistFragment.EXTRA_AUTH_TOKEN, mAuthToken)
                startActivity(FragmentToolbarActivity.intent(WishlistFragment.TAG, args, this))
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

    protected fun setDrawerAccount(account: Account?) {
        if (account == null) {
            mAccountNameView!!.setText(R.string.choose_an_account)
        } else {
            mAccountNameView!!.text = account.name
        }
    }

    val isAuthenticated: Boolean
        get() = mAuthToken != null

    fun showAccountsDialogWithCheck() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        mAccountChooser.showAccountsDialogWithCheck()
    }

    internal class TagsUpdateObserver(private val mDrawerActivity: DrawerActivity) : ContentObserver(Handler()) {

        override fun onChange(selfChange: Boolean) {
            mDrawerActivity.updateTags()
        }
    }
}
