package com.anod.appwatcher.details

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenAsync
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.*
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.*
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import com.squareup.picasso.Picasso
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.anim.RevealAnimatorCompat
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.content.*
import info.anodsplace.framework.graphics.chooseDark
import kotlinx.android.synthetic.main.activity_app_changelog.*
import kotlinx.android.synthetic.main.view_changelog_header.*

abstract class DetailsActivity : ToolbarActivity(), Palette.PaletteAsyncListener, View.OnClickListener, WatchAppList.Listener, AppBarLayout.OnOffsetChangedListener {

    override val themeRes: Int
        get() = Theme(this).themeChangelog

    val viewModel: DetailsViewModel by lazy { ViewModelProviders.of(this).get(DetailsViewModel::class.java) }

    private var addMenu: MenuItem? = null
    private val titleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, this, Color.WHITE).value)
        AlphaSpannableString(viewModel.app!!.title, span)
    }
    private val subtitleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, this, Color.WHITE).value)
        AlphaSpannableString(viewModel.app!!.uploadDate, span)
    }

    val iconLoader: PicassoAppIcon by lazy { App.provide(this).iconLoader }
    private val dataProvider: AppViewHolderResourceProvider by lazy { AppViewHolderResourceProvider(this, InstalledApps.PackageManager(packageManager)) }
    private val appDetailsView: AppDetailsView by lazy { AppDetailsView(container, dataProvider) }
    val adapter: ChangesAdapter by lazy { ChangesAdapter(this) }

    override val layoutResource: Int
        get() = R.layout.activity_app_changelog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: ""
        val detailsUrl = intent.getStringExtra(EXTRA_DETAILS_URL) ?: ""
        val rowId = intent.getIntExtra(EXTRA_ROW_ID, -1)

        progressBar.visibility = View.GONE
        error.visibility = View.GONE
        list.visibility = View.GONE
        background.visibility = View.INVISIBLE
        appbar.addOnOffsetChangedListener(this)

        retryButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            error.visibility = View.GONE
            list.visibility = View.GONE
            retryButton.postDelayed({
                viewModel.loadChangelog()
            }, 500)
        }

        viewModel.detailsUrl = detailsUrl
        viewModel.appId = appId
        viewModel.loadApp(rowId, appId)

        if (viewModel.app == null) {
            Toast.makeText(this, getString(R.string.cannot_load_app, appId), Toast.LENGTH_LONG).show()
            AppLog.e("Cannot loadChangelog app details: '$appId'")
            App.log(this).error("Cannot loadChangelog details for $appId")
            finish()
            return
        }

        viewModel.changelogState.observe(this, Observer {
            val state = it ?: false
            if (state == 2) {
                addMenu?.isEnabled = true
                adapter.setData(viewModel.localChangelog, viewModel.recentChange)
                if (adapter.isEmpty) {
                    showRetryMessage()
                } else {
                    progressBar.visibility = View.GONE
                    list.visibility = View.VISIBLE
                    error.visibility = View.GONE
                }
            }
        })

        setupAppView(viewModel.app!!)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE

        viewModel.account?.let { account ->
            AuthTokenAsync(this).request(this, account, object : AuthTokenAsync.Callback {
                override fun onToken(token: String) {
                    viewModel.authToken = token
                    viewModel.loadChangelog()
                }

                override fun onError(errorMessage: String) {
                    App.log(this@DetailsActivity).error(errorMessage)
                    viewModel.loadChangelog()
                }
            })
        }
    }

    private fun setupAppView(app: AppInfo) {
        playStoreButton.setOnClickListener(this)

        appDetailsView.fillDetails(app, app.rowId == -1)
        supportActionBar?.title = titleString

        if (app.iconUrl.isEmpty()) {
            if (app.rowId > 0) {
                val dbImageUri = DbContentProvider.iconsUri.buildUpon().appendPath(app.rowId.toString()).build()
                iconLoader.retrieve(dbImageUri).into(iconLoadTarget)
            } else {
                setDefaultIcon()
            }
        } else {
            iconLoader.retrieve(app.iconUrl).into(iconLoadTarget)
        }
    }

    private val iconLoadTarget = object : com.squareup.picasso.Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            Palette.from(bitmap).generate(this@DetailsActivity)
            icon.setImageBitmap(bitmap)
            toolbar.logo = BitmapDrawable(resources, bitmap)
            toolbar.logo.alpha = 0
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            AppLog.e("iconLoadTarget::onBitmapFailed", e)
            setDefaultIcon()
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

        }
    }

    private fun setDefaultIcon() {
        val defaultIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_notifications_black_24dp)
        background.visibility = View.VISIBLE
        applyColor(ContextCompat.getColor(this, R.color.theme_primary))
        icon.setImageBitmap(defaultIcon)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.changelog, menu)
        addMenu = menu.findItem(R.id.menu_add)
        addMenu?.isEnabled = false
        if (viewModel.isNewApp) {
            menu.findItem(R.id.menu_remove).isVisible = false
            menu.findItem(R.id.menu_tag_app).isVisible = false
        } else {
            menu.findItem(R.id.menu_add).isVisible = false
            val tagMenu = menu.findItem(R.id.menu_tag_app)
            loadTagSubmenu(tagMenu)
        }
        if (!dataProvider.installedApps.packageInfo(viewModel.appId).isInstalled) {
            menu.findItem(R.id.menu_uninstall).isVisible = false
            menu.findItem(R.id.menu_open).isVisible = false
            menu.findItem(R.id.menu_app_info).isVisible = false
        }

        return true
    }

    private fun loadTagSubmenu(tagMenu: MenuItem) {
        tagMenu.isVisible = false
        viewModel.tags.observe(this, Observer {
            val result = it ?: emptyList()
            if (result.isEmpty()) {
                tagMenu.isVisible = false
            }
            tagMenu.isVisible = true
        })
        viewModel.tagsMenuItems.observe(this, Observer {
            val result = it ?: emptyList()
            val tagSubMenu = tagMenu.subMenu
            for (item in result) {
                tagSubMenu.add(R.id.menu_group_tags, item.first.id, 0, item.first.name).isChecked = item.second
            }
            tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false)
        })
        viewModel.loadTags()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_remove -> {
                val removeDialog = RemoveDialogFragment.newInstance(
                        viewModel.app!!.title, viewModel.app!!.rowId
                )
                removeDialog.show(supportFragmentManager, "removeDialog")
                return true
            }
            R.id.menu_add -> {
                val doc = viewModel.document
                if (doc != null) {
                    val info = AppInfo(doc)
                    val appList = WatchAppList(this)
                    appList.attach(this)
                    appList.add(info)
                    appList.detach()
                }
                return true
            }
            R.id.menu_uninstall -> {
                val data = Intent()
                data.putExtra(EXTRA_UNINSTALL_APP_PACKAGE, viewModel.appId)
                setResult(Activity.RESULT_OK, data)
                val uninstallIntent = Intent().forUninstall(viewModel.appId)
                startActivity(uninstallIntent)
                return true
            }
            R.id.menu_share -> {
                shareApp()
                return true
            }
            R.id.menu_open -> {
                val launchIntent = packageManager.getLaunchIntentForPackage(viewModel.appId)
                if (launchIntent != null) {
                    this.startActivitySafely(launchIntent)
                }
            }
            R.id.menu_app_info -> {
                startActivity(Intent().forAppInfo(viewModel.appId))
            }
        }
        if (item.groupId == R.id.menu_group_tags) {
            if (changeTag(item.itemId, item.isChecked)) {
                item.isChecked = !item.isChecked
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeTag(tagId: Int, checked: Boolean): Boolean {
        val cr = DbContentProviderClient(this)
        if (checked) {
            return cr.removeAppFromTag(viewModel.appId, tagId)
        }
        return cr.addAppToTag(viewModel.appId, tagId)
    }

    private fun shareApp() {
        val appInfo = this.viewModel.app ?: return
        val builder = ShareCompat.IntentBuilder.from(this)

        val changes = if (viewModel.recentChange.details.isBlank()) "" else "${viewModel.recentChange.details}\n\n"
        val text = getString(R.string.share_text, changes ,String.format(Storeintent.URL_WEB_PLAY_STORE, appInfo.packageName))

        builder.setSubject(getString(R.string.share_subject, appInfo.title, appInfo.versionName))
        builder.setText(text)
        builder.setType("text/plain")
        builder.startChooser()
    }

    private fun showRetryMessage() {
        progressBar.visibility = View.GONE
        error.visibility = View.VISIBLE
        list.visibility = View.GONE

        if (!App.provide(this).networkConnection.isNetworkAvailable) {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onGenerated(palette: Palette) {
        val darkSwatch = palette.chooseDark(ContextCompat.getColor(this, R.color.theme_primary))
        applyColor(darkSwatch.rgb)
        animateBackground()

        if (Theme(this).isNightTheme) {
            appDetailsView.updateAccentColor(ContextCompat.getColor(this, R.color.primary_text_dark), viewModel.app!!)
        } else {
            appDetailsView.updateAccentColor(darkSwatch.rgb, viewModel.app!!)
        }
    }

    private fun applyColor(@ColorInt color: Int) {
        val drawable = DrawableCompat.wrap(playStoreButton.drawable)
        DrawableCompat.setTint(drawable, color)
        playStoreButton.setImageDrawable(drawable)
        background.setBackgroundColor(color)
        progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun animateBackground() {
        background.post {
            val location = IntArray(2)
            icon.getLocationOnScreen(location)
            if (ViewCompat.isAttachedToWindow(background)) {
                RevealAnimatorCompat.show(background, location[0], location[1], 0).start()
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.playStoreButton) {
            val intent = Intent().forPlayStore(viewModel.app!!.packageName)
            this.startActivitySafely(intent)
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        val data = Intent()
        data.putExtra(EXTRA_ADD_APP_PACKAGE, info.packageName)
        setResult(Activity.RESULT_OK, data)

        sendBroadcast(Intent(AddWatchAppAsyncTask.listChanged))

        TagSnackbar.make(this, info, true).show()
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
        val alpha = 1.0f - Math.abs(verticalOffset.toFloat() / totalScrollRange)

        header.alpha = alpha
        playStoreButton.alpha = alpha
        playStoreButton.isEnabled = alpha > 0.8f

        val inverseAlpha = (1.0f - alpha)
        toolbar.logo?.alpha = (inverseAlpha * 255).toInt()
        titleString.alpha = inverseAlpha
        subtitleString.alpha = inverseAlpha
        mainHandler.post({
            supportActionBar?.title = titleString
            toolbar.subtitle = subtitleString
            playStoreButton.translationY = verticalOffset.toFloat()
        })
    }


    companion object {
        const val EXTRA_APP_ID = "app_id"
        const val EXTRA_DETAILS_URL = "url"
        const val EXTRA_ROW_ID = "row_id"
        const val EXTRA_ADD_APP_PACKAGE = "app_add_success"
        const val EXTRA_UNINSTALL_APP_PACKAGE = "app_uninstall"
    }
}
