package com.anod.appwatcher.details

import android.app.Activity
import android.content.Intent
import android.database.Cursor
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
import android.support.v4.app.LoaderManager
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
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
import com.anod.appwatcher.content.AppChangeCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.*
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.*
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import com.squareup.picasso.Picasso
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.anim.RevealAnimatorCompat
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.content.EmptyLoader
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.database.NullCursor
import info.anodsplace.framework.graphics.chooseDark
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.framework.os.CachedBackgroundTask
import info.anodsplace.playstore.DetailsEndpoint
import kotlinx.android.synthetic.main.activity_app_changelog.*
import kotlinx.android.synthetic.main.view_changelog_header.*

open class DetailsActivity : ToolbarActivity(), Palette.PaletteAsyncListener, View.OnClickListener, WatchAppList.Listener, LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {

    override val themeRes: Int
        get() = Theme(this).themeChangelog

    private var detailsUrl: String = ""
    var appId: String = ""

    private var appInfo: AppInfo? = null
    private var isNewApp: Boolean = false
    private var addMenu: MenuItem? = null
    private val titleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, this, Color.WHITE).value)
        AlphaSpannableString(appInfo!!.title, span)
    }
    private val subtitleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, this, Color.WHITE).value)
        AlphaSpannableString(appInfo!!.uploadDate, span)
    }
    private var detailsEndpoint: DetailsEndpoint? = null

    val iconLoader: PicassoAppIcon by lazy { App.provide(this).iconLoader }
    val dataProvider: AppViewHolderResourceProvider by lazy { AppViewHolderResourceProvider(this, InstalledApps.PackageManager(packageManager)) }
    val appDetailsView: AppDetailsView by lazy { AppDetailsView(container, dataProvider) }
    val adapter: ChangesAdapter by lazy { ChangesAdapter(this, AppChange(appId, 0, "", "", "") ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_changelog)
        setupToolbar()

        appId = intent.getStringExtra(EXTRA_APP_ID) ?: ""
        detailsUrl = intent.getStringExtra(EXTRA_DETAILS_URL) ?: ""
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
                supportLoaderManager.initLoader(0, null, this).forceLoad()
            }, 500)
        }

        if (rowId == -1) {
            appInfo = packageManager.packageToApp(-1, appId)
            isNewApp = true
            App.log(this).info("Show details for unwatched $appId")
        } else {
            val cr = DbContentProviderClient(this)
            appInfo = cr.queryAppRow(rowId)
            cr.close()
            isNewApp = false
            App.log(this).info("Show details for watched $appId")
        }

        if (appInfo == null) {
            Toast.makeText(this, getString(R.string.cannot_load_app, appId), Toast.LENGTH_LONG).show()
            AppLog.e("Cannot load app details: '$appId'")
            App.log(this).error("Cannot load details for $appId")
            finish()
            return
        }
        setupAppView(appInfo!!)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE

        val requestQueue = App.provide(this).requestQueue
        val deviceInfo = App.provide(this).deviceInfo

        App.provide(this).prefs.account?.let { account ->
            AuthTokenAsync(this).request(this, account, object : AuthTokenAsync.Callback {
                override fun onToken(token: String) {
                    detailsEndpoint = DetailsEndpoint(this@DetailsActivity, requestQueue, deviceInfo, account, detailsUrl)
                    detailsEndpoint!!.authToken = token

                    supportLoaderManager.initLoader(0, null, this@DetailsActivity).forceLoad()
                }

                override fun onError(errorMessage: String) {
                    App.log(this@DetailsActivity).error(errorMessage)
                    if (App.provide(this@DetailsActivity).networkConnection.isNetworkAvailable) {
                        showRetryMessage()
                    } else {
                        supportLoaderManager.initLoader(0, null, this@DetailsActivity).forceLoad()
                    }
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
        if (isNewApp) {
            menu.findItem(R.id.menu_remove).isVisible = false
            menu.findItem(R.id.menu_tag_app).isVisible = false
        } else {
            menu.findItem(R.id.menu_add).isVisible = false
            val tagMenu = menu.findItem(R.id.menu_tag_app)
            loadTagSubmenu(tagMenu)
        }
        if (!dataProvider.installedApps.packageInfo(appId).isInstalled) {
            menu.findItem(R.id.menu_uninstall).isVisible = false
            menu.findItem(R.id.menu_open).isVisible = false
        }

        return true
    }

    private class TagMenuItem constructor(internal val tag: Tag, internal val selected: Boolean)

    private fun loadTagSubmenu(tagMenu: MenuItem) {
        tagMenu.isVisible = false
        CachedBackgroundTask("tags", object : BackgroundTask.Worker<Void?, List<Tag>>(null) {

            override fun run(param: Void?): List<Tag> {
                val cr = DbContentProviderClient(this@DetailsActivity)
                val tags = cr.queryTags()
                cr.close()
                return tags.toList()
            }

            override fun finished(result: List<Tag>) {
                if (result.isEmpty()) {
                    tagMenu.isVisible = false
                    return
                }
                tagMenu.isVisible = true
                BackgroundTask(object : BackgroundTask.Worker<List<Tag>, List<TagMenuItem>>(result) {
                    override fun run(param: List<Tag>): List<TagMenuItem> {
                        val cr = DbContentProviderClient(this@DetailsActivity)
                        val appTags = cr.queryAppTags(appInfo!!.rowId)
                        cr.close()
                        return param.map { TagMenuItem(it, appTags.contains(it.id)) }
                    }

                    override fun finished(result: List<TagMenuItem>) {
                        val tagSubMenu = tagMenu.subMenu
                        for (item in result) {
                            tagSubMenu.add(R.id.menu_group_tags, item.tag.id, 0, item.tag.name).isChecked = item.selected
                        }
                        tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false)
                    }
                }).execute()
            }
        }, ApplicationContext(this)).execute()

    }

    val changesLoader: ChangesLoader?
        get() = supportLoaderManager.getLoader<Cursor?>(0) as? ChangesLoader

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_remove -> {
                val removeDialog = RemoveDialogFragment.newInstance(
                        appInfo!!.title, appInfo!!.rowId
                )
                removeDialog.show(supportFragmentManager, "removeDialog")
                return true
            }
            R.id.menu_add -> {

                val doc = changesLoader?.document
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
                data.putExtra(EXTRA_UNINSTALL_APP_PACKAGE, appId)
                setResult(Activity.RESULT_OK, data)
                val uninstallIntent = Intent().forUninstall(appId)
                startActivity(uninstallIntent)
                return true
            }
            R.id.menu_share -> {
                shareApp()
                return true
            }
            R.id.menu_open -> {
                val launchIntent = packageManager.getLaunchIntentForPackage(appId)
                if (launchIntent != null) {
                    this.startActivitySafely(launchIntent)
                }
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
            return cr.removeAppFromTag(appInfo!!.appId, tagId)
        }
        return cr.addAppToTag(appInfo!!.appId, tagId)
    }

    private fun shareApp() {
        val appInfo = this.appInfo ?: return
        val builder = ShareCompat.IntentBuilder.from(this)

        val changes = if (adapter.recentChange.details.isBlank()) "" else "${adapter.recentChange.details}\n\n"
        val text = getString(R.string.share_text, changes ,String.format(Storeintent.URL_WEB_PLAY_STORE, appInfo.packageName))

        builder.setSubject(getString(R.string.share_subject, appInfo.title, appInfo.versionName))
        builder.setText(text)
        builder.setType("text/plain")
        builder.startChooser()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return if (appId.isBlank())
            EmptyLoader(this, NullCursor())
        else ChangesLoader(this, appId, detailsEndpoint)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.swapData(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        val changesLoader = (loader as? ChangesLoader)
        if (changesLoader == null) { // EmptyLoader
            showRetryMessage()
            return
        }
        val cursor = data as? AppChangeCursor
        if (changesLoader.document == null && (cursor == null || !cursor.hasNext()) ) {
            showRetryMessage()
            return
        }
        addMenu?.isEnabled = true
        adapter.swapData(cursor)
        adapter.recentChange = changesLoader.recentChange
        progressBar.visibility = View.GONE
        list.visibility = View.VISIBLE
        error.visibility = View.GONE
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
            appDetailsView.updateAccentColor(ContextCompat.getColor(this, R.color.primary_text_dark), appInfo!!)
        } else {
            appDetailsView.updateAccentColor(darkSwatch.rgb, appInfo!!)
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
            val intent = Intent().forPlayStore(appInfo!!.packageName)
            this.startActivitySafely(intent)
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        val data = Intent()
        data.putExtra(EXTRA_ADD_APP_PACKAGE, info.packageName)
        setResult(Activity.RESULT_OK, data)

        TagSnackbar.make(this, info, true).show()
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }

    val mainHandler = Handler(Looper.getMainLooper())

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
