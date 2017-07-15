package com.anod.appwatcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.graphics.Palette
import android.text.Html
import android.text.TextUtils
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.android.volley.VolleyError
import com.anod.appwatcher.accounts.AuthTokenProvider
import com.anod.appwatcher.adapters.AppDetailsView
import com.anod.appwatcher.adapters.AppViewHolderDataProvider
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.fragments.RemoveDialogFragment
import com.anod.appwatcher.market.DetailsEndpoint
import com.anod.appwatcher.market.MarketInfo
import com.anod.appwatcher.market.PlayStoreEndpoint
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.ui.ToolbarActivity
import com.anod.appwatcher.utils.*
import com.squareup.picasso.Picasso
import info.anodsplace.android.anim.RevealAnimatorCompat
import info.anodsplace.android.log.AppLog
import java.util.*


class ChangelogActivity : ToolbarActivity(), PlayStoreEndpoint.Listener, Palette.PaletteAsyncListener, View.OnClickListener, WatchAppList.Listener {

    @BindView(R.id.progress_bar)
    lateinit var mLoadingView: ProgressBar
    @BindView(R.id.changelog)
    lateinit var mChangelog: TextView
    @BindView(R.id.retry)
    lateinit var mRetryButton: Button
    @BindView(android.R.id.icon)
    lateinit var mAppIcon: ImageView
    @BindView(R.id.background)
    lateinit var mBackground: View
    @BindView(R.id.market_btn)
    lateinit var mPlayStoreButton: FloatingActionButton
    @BindView(R.id.content)
    lateinit var mContent: View

    private var mDetailsUrl: String = ""
    var mAppId: String = ""

    lateinit var mDetailsEndpoint: DetailsEndpoint
    private var mApp: AppInfo? = null
    private var mNewApp: Boolean = false
    private var mAddMenu: MenuItem? = null
    lateinit var mIconLoader: AppIconLoader
    lateinit var mDataProvider: AppViewHolderDataProvider
    lateinit var mAppDetailsView: AppDetailsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_changelog)
        ButterKnife.bind(this)
        setupToolbar()

        mIconLoader = App.provide(this).iconLoader
        mAppId = intent.getStringExtra(EXTRA_APP_ID)
        mDetailsUrl = intent.getStringExtra(EXTRA_DETAILS_URL)
        val rowId = intent.getIntExtra(EXTRA_ROW_ID, -1)
        MetricsManagerEvent.track(this, "open_changelog", "DETAILS_APP_ID", mAppId, "DETAILS_ROW_ID", rowId.toString())

        mDataProvider = AppViewHolderDataProvider(this, InstalledAppsProvider.PackageManager(packageManager))
        val contentView = findViewById<View>(R.id.container)
        mAppDetailsView = AppDetailsView(contentView, mDataProvider)

        mDetailsEndpoint = DetailsEndpoint(this)
        mDetailsEndpoint.url = mDetailsUrl

        mContent.visibility = View.INVISIBLE
        mLoadingView.visibility = View.GONE
        mRetryButton.visibility = View.GONE
        mChangelog.visibility = View.GONE
        mBackground.visibility = View.INVISIBLE

        mRetryButton.setOnClickListener {
            mLoadingView.visibility = View.VISIBLE
            mRetryButton.visibility = View.GONE
            mChangelog.visibility = View.GONE
            mRetryButton.postDelayed({ mDetailsEndpoint.startAsync() }, 500)
        }

        if (rowId == -1) {
            mApp = loadInstalledApp()
            mNewApp = true
        } else {
            val cr = DbContentProviderClient(this)
            mApp = cr.queryAppRow(rowId)
            cr.close()
            mNewApp = false
        }

        if (mApp == null) {
            Toast.makeText(this, getString(R.string.cannot_load_app, mAppId), Toast.LENGTH_LONG).show()
            AppLog.e("Cannot load app details: '$mAppId'")
            finish()
            return
        }
        setupAppView(mApp!!)
    }

    private fun loadInstalledApp(): AppInfo {
        return PackageManagerUtils.packageToApp(mAppId, packageManager)
    }

    override fun onResume() {
        super.onResume()
        mDetailsEndpoint.listener = this
        mLoadingView.visibility = View.VISIBLE

        App.provide(this).prefs.account?.let {
            AuthTokenProvider(this).requestToken(this, it, object : AuthTokenProvider.AuthenticateCallback {
                override fun onAuthTokenAvailable(token: String) {
                    mDetailsEndpoint.setAccount(it, token)
                    mDetailsEndpoint.startAsync()
                }

                override fun onUnRecoverableException(errorMessage: String) {
                    showRetryMessage()
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mDetailsEndpoint.listener = null
    }

    private fun setupAppView(app: AppInfo) {
        mPlayStoreButton.setOnClickListener(this)

        mAppDetailsView.fillDetails(app, app.rowId == -1)

        if (app.iconUrl.isEmpty()) {
            if (app.rowId > 0) {
                val dbImageUri = DbContentProvider.ICONS_CONTENT_URI.buildUpon().appendPath(app.rowId.toString()).build()
                mIconLoader.retrieve(dbImageUri).into(mIconLoadTarget)
            } else {
                setDefaultIcon()
            }
        } else {
            mIconLoader.retrieve(app.iconUrl).into(mIconLoadTarget)
        }
    }

    private val mIconLoadTarget = object : com.squareup.picasso.Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            Palette.from(bitmap).generate(this@ChangelogActivity)
            mAppIcon.setImageBitmap(bitmap)
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            AppLog.e("mIconLoadTarget::onBitmapFailed", e)
            setDefaultIcon()
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

        }
    }

    private fun setDefaultIcon() {
        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_notifications_black_24dp)
        mBackground.visibility = View.VISIBLE
        applyColor(ContextCompat.getColor(this, R.color.theme_primary))
        mAppIcon.setImageBitmap(icon)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.changelog, menu)
        mAddMenu = menu.findItem(R.id.menu_add)
        mAddMenu!!.isEnabled = false
        if (mNewApp) {
            menu.findItem(R.id.menu_remove).isVisible = false
            menu.findItem(R.id.menu_tag_app).isVisible = false
        } else {
            menu.findItem(R.id.menu_add).isVisible = false
            val tagMenu = menu.findItem(R.id.menu_tag_app)
            loadTagSubmenu(tagMenu)
        }
        if (!mDataProvider.installedAppsProvider.getInfo(mAppId).isInstalled) {
            menu.findItem(R.id.menu_uninstall).isVisible = false
        }

        return true
    }

    private class TagMenuItem constructor(internal val tag: Tag, internal val selected: Boolean)

    private fun loadTagSubmenu(tagMenu: MenuItem) {

        BackgroundTask.execute(object : BackgroundTask.Worker<Void?, List<TagMenuItem>>(null) {

            override fun run(param: Void?): List<TagMenuItem> {
                val cr = DbContentProviderClient(this@ChangelogActivity)
                val tags = cr.queryTags()
                val appTags = cr.queryAppTags(mApp!!.rowId)

                tags.moveToPosition(-1)
                val result = ArrayList<TagMenuItem>()
                while (tags.moveToNext()) {
                    val tag = tags.tag
                    result.add(TagMenuItem(tag, appTags.contains(tag.id)))
                }
                cr.close()
                return result
            }

            override fun finished(result: List<TagMenuItem>) {
                val tagSubMenu = tagMenu.subMenu
                for (item in result) {
                    tagSubMenu.add(R.id.menu_group_tags, item.tag.id, 0, item.tag.name).isChecked = item.selected
                }
                tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false)
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_remove -> {
                val removeDialog = RemoveDialogFragment.newInstance(
                        mApp!!.title, mApp!!.rowId
                )
                removeDialog.show(supportFragmentManager, "removeDialog")
                return true
            }
            R.id.menu_add -> {
                val doc = mDetailsEndpoint.document
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
                data.putExtra(EXTRA_UNINSTALL_APP_PACKAGE, mAppId)
                setResult(Activity.RESULT_OK, data)
                val uninstallIntent = IntentUtils.createUninstallIntent(mAppId)
                startActivity(uninstallIntent)
                return true
            }
            R.id.menu_share -> {
                shareApp()
                return true
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
            return cr.removeAppFromTag(mApp!!.appId, tagId)
        }
        return cr.addAppToTag(mApp!!.appId, tagId)
    }

    private fun shareApp() {
        val builder = ShareCompat.IntentBuilder.from(this)
        if (mApp!!.status == AppInfoMetadata.STATUS_UPDATED) {
            builder.setSubject(getString(R.string.share_subject_updated, mApp!!.title))
        } else {
            builder.setSubject(getString(R.string.share_subject_normal, mApp!!.title))
        }
        builder.setText(String.format(MarketInfo.URL_WEB_PLAY_STORE, mApp!!.packageName))
        builder.setType("text/plain")
        builder.startChooser()
    }

    override fun onDataChanged() {
        mLoadingView.visibility = View.GONE
        mContent.visibility = View.VISIBLE
        mChangelog.visibility = View.VISIBLE
        mChangelog.autoLinkMask = Linkify.ALL

        mRetryButton.visibility = View.GONE
        val changes = mDetailsEndpoint.recentChanges
        if (changes.isEmpty()) {
            mChangelog.setText(R.string.no_recent_changes)
        } else {
            mChangelog.text = Html.fromHtml(changes)
        }
        if (mDetailsEndpoint.document != null) {
            mAddMenu!!.isEnabled = true
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        showRetryMessage()
    }

    private fun showRetryMessage() {
        mContent.visibility = View.VISIBLE
        mLoadingView.visibility = View.GONE
        mChangelog.visibility = View.VISIBLE
        mChangelog.autoLinkMask = Linkify.ALL

        mChangelog.text = getString(R.string.error_fetching_info)
        mRetryButton.visibility = View.VISIBLE
        if (!App.with(this).isNetworkAvailable) {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onGenerated(palette: Palette) {
        val darkSwatch = PaletteSwatch.getDark(palette, ContextCompat.getColor(this, R.color.theme_primary))
        applyColor(darkSwatch.rgb)
        animateBackground()

        if (App.with(this).isNightTheme) {
            mAppDetailsView.updateAccentColor(ContextCompat.getColor(this, R.color.primary_text_dark), mApp!!)
        } else {
            mAppDetailsView.updateAccentColor(darkSwatch.rgb, mApp!!)
        }
    }

    private fun applyColor(@ColorInt color: Int) {
        val drawable = DrawableCompat.wrap(mPlayStoreButton.drawable)
        DrawableCompat.setTint(drawable, color)
        mPlayStoreButton.setImageDrawable(drawable)
        mBackground.setBackgroundColor(color)
        mLoadingView.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun animateBackground() {
        mBackground.post {
            val location = IntArray(2)
            mAppIcon.getLocationOnScreen(location)
            if (ViewCompat.isAttachedToWindow(mBackground)) {
                RevealAnimatorCompat.show(mBackground, location[0], location[1], 0).start()
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.market_btn) {
            val intent = IntentUtils.createPlayStoreIntent(mApp!!.packageName)
            startActivity(intent)
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

    companion object {
        const val EXTRA_APP_ID = "app_id"
        const val EXTRA_DETAILS_URL = "url"
        const val EXTRA_ROW_ID = "row_id"
        const val EXTRA_ADD_APP_PACKAGE = "app_add_success"
        const val EXTRA_UNINSTALL_APP_PACKAGE = "app_uninstall"
    }
}
