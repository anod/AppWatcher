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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.accounts.AccountSelectionDialog
import com.anod.appwatcher.accounts.AccountSelectionResult
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.InstalledActivity
import com.anod.appwatcher.tags.EditTagDialog
import com.anod.appwatcher.tags.TagWatchListComposeActivity
import com.anod.appwatcher.utils.*
import com.anod.appwatcher.wishlist.WishListActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.util.Hash
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.permissions.toRequestInput
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Alex Gavrishev
 * @date 01/12/2017
 */
abstract class DrawerActivity : ToolbarActivity(), KoinComponent {


    override val themeRes: Int
        get() = Theme(this, prefs).theme

    private val drawerLayout: DrawerLayout? by lazy { findViewById(R.id.drawer_layout) }
    private val navigationView: NavigationView? by lazy { findViewById(R.id.nav_view) }
    private val accountNameView: TextView? by lazy { navigationView?.getHeaderView(0)?.findViewById(R.id.account_name) }
    private val drawerViewModel: DrawerViewModel by viewModels()
    val authToken: AuthTokenBlocking by inject()
    private lateinit var notificationPermissionRequest: ActivityResultLauncher<AppPermissions.Request.Input>

    open val isHomeAsMenu: Boolean
        get() = false

    fun showAccountsDialogWithCheck() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        accountSelectionDialog.show()
    }

    private lateinit var accountSelectionDialog: AccountSelectionDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDrawer()

        accountSelectionDialog = AccountSelectionDialog(this, prefs)
        notificationPermissionRequest = registerForActivityResult(AppPermissions.Request()) {
            val enabled = it[AppPermission.PostNotification.value] ?: false
            if (!enabled && prefs.notificationDisabledToastCount < 3) {
                Toast.makeText(this, R.string.notifications_not_enabled, Toast.LENGTH_LONG).show()
                prefs.notificationDisabledToastCount = prefs.notificationDisabledToastCount + 1
            }
            if (enabled) {
                onNotificationEnabled()
            }
        }

        lifecycleScope.launchWhenCreated {
            accountSelectionDialog.accountSelected.collect { result ->
                when (result) {
                    AccountSelectionResult.Canceled -> onAccountNotFound("")
                    is AccountSelectionResult.Error -> onAccountNotFound(result.errorMessage)
                    is AccountSelectionResult.Success -> onAccountSelected(result.account)
                }
            }
        }

        if (account == null) {
            accountSelectionDialog.show()
        } else {
            onAccountSelected(account!!)
        }
    }

    open fun onNotificationEnabled() {}

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

        viewModel.account.observe(this) {
            updateDrawerAccount(it)
        }

        viewModel.lastUpdateTime.observe(this) {
            updateLastUpdateTime(it ?: 0)
        }

        lifecycleScope.launch {
            viewModel.tags.collectLatest {
                updateTags(it)
            }
        }

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
        val addTag = menu.add(1, -1, Menu.NONE, R.string.tags)
        addTag.setActionView(R.layout.drawer_tag_add)
        val addTagButton = addTag.actionView!!.findViewById<View>(R.id.button1)
        addTagButton.setOnClickListener {
            EditTagDialog.show(supportFragmentManager, null, Theme(this, prefs))
        }
        addTag.setOnMenuItemClickListener {
            EditTagDialog.show(supportFragmentManager, null, Theme(this, prefs))
            true
        }
        result.forEach { (tag, count) ->
            val item = menu.add(1, tag.id, Menu.NONE, tag.name)
            item.setActionView(R.layout.drawer_tag_indicator)
            val tagIndicator = item.actionView!!.findViewById<View>(android.R.id.text1) as TextView
            val d = ResourcesCompat.getDrawable(resources, R.drawable.circular_color, null)
            DrawableCompat.setTint(DrawableCompat.wrap(d!!), tag.color)
            tagIndicator.background = d
            tagIndicator.text = if (count > 99) "99+" else "" + count
            if (tag.isLightColor) {
                tagIndicator.setTextColor(ResourcesCompat.getColor(resources, R.color.alwaysBlack, theme))
            } else {
                tagIndicator.setTextColor(ResourcesCompat.getColor(resources, R.color.alwaysWhite, theme))
            }
            item.intent = TagWatchListComposeActivity.createTagIntent(tag, this@DrawerActivity)
        }
        memoryCache.put("tags", result.map { it.first })
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
            R.id.menu_act_installed -> {
                startActivity(InstalledActivity.intent(
                        false,
                        this))
                return true
            }
            R.id.menu_wishlist -> {
                startActivity(WishListActivity.intent(
                        this,
                        account, authToken.token))
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

    protected open fun onAccountSelected(account: Account) {
        drawerViewModel.account.value = account
        val collectReports = prefs.collectCrashReports
        lifecycleScope.launch {
            try {
                if (authToken.refreshToken(account)) {
                    if (collectReports) {
                        FirebaseCrashlytics.getInstance().setUserId(Hash.sha256(account.name).encoded)
                    }
                    updateDrawerAccount(account)
                    if (!prefs.areNotificationsEnabled && prefs.updatesFrequency > 0) {
                        notificationPermissionRequest.launch(AppPermission.PostNotification.toRequestInput())
                    }
                } else {
                    AppLog.e("Error retrieving authentication token")
                    if (networkConnection.isNetworkAvailable) {
                        Toast.makeText(this@DrawerActivity, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@DrawerActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: AuthTokenStartIntent) {
                startActivity(e.intent)
            }
        }
    }

    private fun onAccountNotFound(errorMessage: String) {
        if (networkConnection.isNetworkAvailable) {
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