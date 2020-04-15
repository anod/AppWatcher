// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.details

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.*
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.anim.RevealAnimatorCompat
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.graphics.chooseDark
import kotlinx.android.synthetic.main.fragment_app_changelog.*
import kotlinx.android.synthetic.main.view_changelog_header.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class DetailsFragment : Fragment(), Palette.PaletteAsyncListener, View.OnClickListener, AppBarLayout.OnOffsetChangedListener, Toolbar.OnMenuItemClickListener {

    private var loaded = false
    private val viewModel: DetailsViewModel by viewModels()
    private var addMenu: MenuItem? = null
    private val titleString: AlphaSpannableString
        get() {
            val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, requireContext(), Color.WHITE).value)
            return AlphaSpannableString(viewModel.app.value!!.generateTitle(resources), span)
        }

    private val subtitleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, requireContext(), Color.WHITE).value)
        AlphaSpannableString(viewModel.app.value!!.uploadDate, span)
    }

    private val iconLoader: PicassoAppIcon
        get() = Application.provide(this).iconLoader

    private val dataProvider: AppViewHolderResourceProvider by lazy {
        AppViewHolderResourceProvider(requireContext(), InstalledApps.PackageManager(requireContext().packageManager))
    }

    private val appDetailsView: AppDetailsView by lazy { AppDetailsView(container, dataProvider) }

    private val adapter: ChangesAdapter by lazy { ChangesAdapter(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_app_changelog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.appId.value = requireArguments().getString(DetailsActivity.EXTRA_APP_ID) ?: ""
        viewModel.detailsUrl = requireArguments().getString(DetailsActivity.EXTRA_DETAILS_URL) ?: ""
        viewModel.rowId = requireArguments().getInt(DetailsActivity.EXTRA_ROW_ID, -1)

        setupToolbar()
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
                viewModel.loadRemoteChangelog()
            }, 500)
        }

        if (viewModel.appId.value!!.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.cannot_load_app, viewModel.appId), Toast.LENGTH_LONG).show()
            AppLog.e("Cannot loadChangelog app details: '${viewModel.appId}'")
            progressBar.visibility = View.GONE
            error.visibility = View.VISIBLE
            return
        }

        viewModel.changelogState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Complete -> {
                    addMenu?.isEnabled = true
                    viewModel.app.value?.let { app ->
                        appDetailsView.title.text = app.generateTitle(resources)
                    }
                    adapter.setData(viewModel.localChangelog, viewModel.recentChange)
                    if (adapter.isEmpty) {
                        showRetryMessage()
                    } else {
                        progressBar.visibility = View.GONE
                        list.visibility = View.VISIBLE
                        error.visibility = View.GONE
                    }
                }
                else -> {
                    progressBar.visibility = View.GONE
                    list.visibility = View.VISIBLE
                    error.visibility = View.GONE
                    adapter.setData(viewModel.localChangelog, viewModel.recentChange)
                }
            }
        })

        viewModel.app.observe(viewLifecycleOwner, Observer { app ->
            if (app == null) {
                Toast.makeText(requireContext(), getString(R.string.cannot_load_app, viewModel.appId), Toast.LENGTH_LONG).show()
                AppLog.e("Cannot loadChangelog app details: '${viewModel.appId}'")
                progressBar.visibility = View.GONE
                error.visibility = View.VISIBLE
            } else {
                loaded = true
                setupAppView(app)
                list.layoutManager = LinearLayoutManager(requireContext())
                list.adapter = adapter
            }
        })

        viewModel.watchStateChange.observe(viewLifecycleOwner) { result ->
            when (result) {
                AppListTable.ERROR_ALREADY_ADDED -> Toast.makeText(requireContext(), R.string.app_already_added, Toast.LENGTH_SHORT).show()
                AppListTable.ERROR_INSERT -> Toast.makeText(requireContext(), R.string.error_insert_app, Toast.LENGTH_SHORT).show()
                else -> {
                    val info = AppInfo(viewModel.document!!)
                    val finishActivity = activity is DetailsActivity
                    TagSnackbar.make(requireActivity(), info, finishActivity).show()
                }
            }
        }

        viewModel.loadApp()
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE

        viewModel.loadLocalChangelog()
        viewModel.account?.let { account ->
            lifecycleScope.launch {
                try {
                    val token = AuthTokenBlocking(requireContext().applicationContext).retrieve(account)
                    if (token.isNotBlank()) {
                        viewModel.authToken = token
                        viewModel.loadRemoteChangelog()
                    } else {
                        AppLog.e("Error retrieving token")
                        viewModel.loadRemoteChangelog()
                    }
                } catch (e: AuthTokenStartIntent) {
                    startActivity(e.intent)
                }
            }
        }
    }

    private fun setupAppView(app: App) {
        playStoreButton.setOnClickListener(this)

        appDetailsView.fillDetails(app, false, "", false, app.rowId == -1)
        toolbar.title = titleString

        if (app.iconUrl.isEmpty()) {
            setDefaultIcon()
        } else {
            iconLoader.retrieve(app.iconUrl).into(iconLoadTarget)
        }
    }

    private val iconLoadTarget = object : com.squareup.picasso.Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            Palette.from(bitmap).generate(this@DetailsFragment)
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
        background.visibility = View.VISIBLE
        applyColor(ContextCompat.getColor(requireContext(), R.color.theme_accent))
        icon.setImageResource(R.drawable.ic_notifications_black_24dp)
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.changelog)
        val menu = toolbar.menu
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
        if (!dataProvider.installedApps.packageInfo(viewModel.appId.value!!).isInstalled) {
            menu.findItem(R.id.menu_uninstall).isVisible = false
            menu.findItem(R.id.menu_open).isVisible = false
            menu.findItem(R.id.menu_app_info).isVisible = false
        }

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_white_24)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        toolbar.setOnMenuItemClickListener(this)
    }

    private fun loadTagSubmenu(tagMenu: MenuItem) {
        tagMenu.isVisible = false
        viewModel.tagsMenuItems.observe(viewLifecycleOwner, Observer {
            val result = it ?: emptyList()
            tagMenu.isVisible = result.isNotEmpty()
            val tagSubMenu = tagMenu.subMenu
            tagSubMenu.removeGroup(R.id.menu_group_tags)
            for (item in result) {
                tagSubMenu.add(R.id.menu_group_tags, item.first.id, 0, item.first.name).isChecked = item.second
            }
            tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false)
        })
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_remove -> {
                val removeDialog = RemoveDialogFragment.newInstance(
                        viewModel.app.value!!.title, viewModel.app.value!!.rowId
                )
                removeDialog.show(childFragmentManager, "removeDialog")
                return true
            }
            R.id.menu_add -> {
                viewModel.watch()
                return true
            }
            R.id.menu_uninstall -> {
                val uninstallIntent = Intent().forUninstall(viewModel.appId.value!!)
                startActivity(uninstallIntent)
                return true
            }
            R.id.menu_share -> {
                shareApp()
                return true
            }
            R.id.menu_open -> {
                val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(viewModel.appId.value!!)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                    startActivitySafely(launchIntent)
                }
            }
            R.id.menu_app_info -> {
                startActivity(Intent().forAppInfo(viewModel.appId.value!!))
            }
        }
        if (item.groupId == R.id.menu_group_tags) {
            viewModel.changeTag(item.itemId, item.isChecked)
        }

        return false
    }

    private fun shareApp() {
        val appInfo = this.viewModel.app.value ?: return
        val builder = ShareCompat.IntentBuilder.from(requireActivity())

        val changes = if (viewModel.recentChange.details.isBlank()) "" else "${viewModel.recentChange.details}\n\n"
        val text = getString(R.string.share_text, changes, String.format(StoreIntent.URL_WEB_PLAY_STORE, appInfo.packageName))

        builder.setSubject(getString(R.string.share_subject, appInfo.title, appInfo.versionName))
        builder.setText(text)
        builder.setType("text/plain")
        builder.startChooser()
    }

    private fun showRetryMessage() {
        progressBar.visibility = View.GONE
        error.visibility = View.VISIBLE
        list.visibility = View.GONE

        if (!Application.provide(this).networkConnection.isNetworkAvailable) {
            Toast.makeText(requireContext(), R.string.check_connection, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onGenerated(palette: Palette?) {
        val context = context ?: return
        val defaultColor = ContextCompat.getColor(context, R.color.theme_primary)
        val darkSwatch = palette?.chooseDark(defaultColor) ?: Palette.Swatch(defaultColor, 0)
        applyColor(darkSwatch.rgb)
        animateBackground()

        if (Theme(requireActivity()).isNightTheme) {
            appDetailsView.updateAccentColor(ContextCompat.getColor(requireContext(), R.color.primary_text_dark))
        } else {
            appDetailsView.updateAccentColor(darkSwatch.rgb)
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
            val intent = Intent().forPlayStore(viewModel.app.value!!.packageName)
            this.startActivitySafely(intent)
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
        val alpha = 1.0f - abs(verticalOffset.toFloat() / totalScrollRange)

        if (!loaded) {
            return
        }

        header.alpha = alpha
        playStoreButton.alpha = alpha
        playStoreButton.isEnabled = alpha > 0.8f

        val inverseAlpha = (1.0f - alpha)
        toolbar.logo?.alpha = (inverseAlpha * 255).toInt()
        titleString.alpha = inverseAlpha
        subtitleString.alpha = inverseAlpha
        container.post {
            toolbar?.let {
                it.title = titleString
                it.subtitle = subtitleString
                playStoreButton.translationY = verticalOffset.toFloat()
            }
        }
    }

    companion object {
        const val tag = "DetailsFragment"
        fun newInstance(appId: String, detailsUrl: String, rowId: Int) = DetailsFragment().apply {
            arguments = Bundle().apply {
                putString(DetailsActivity.EXTRA_APP_ID, appId)
                putString(DetailsActivity.EXTRA_DETAILS_URL, detailsUrl)
                putInt(DetailsActivity.EXTRA_ROW_ID, rowId)
            }
        }
    }

}