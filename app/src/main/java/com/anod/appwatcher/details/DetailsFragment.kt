// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.details

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.databinding.FragmentAppChangelogBinding
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.provide
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.*
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import com.google.android.material.appbar.AppBarLayout
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.anim.RevealAnimatorCompat
import info.anodsplace.framework.app.addMultiWindowFlags
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.graphics.chooseDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class DetailsFragment : Fragment(), View.OnClickListener, AppBarLayout.OnOffsetChangedListener, Toolbar.OnMenuItemClickListener {

    private var loaded = false
    private val viewModel: DetailsViewModel by viewModels()
    private var toggleMenu: MenuItem? = null

    private val titleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(ColorAttribute(android.R.attr.textColor, requireContext(), Color.WHITE).value)
        AlphaSpannableString(viewModel.app.value!!.generateTitle(resources), span)
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

    private val appDetailsView: AppDetailsView by lazy { AppDetailsView(binding.container, dataProvider) }

    private val adapter: ChangesAdapter by lazy { ChangesAdapter(requireContext()) }

    private var _binding: FragmentAppChangelogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAppChangelogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.detailsUrl = requireArguments().getString(extraDetailsUrl) ?: ""
        viewModel.rowId = requireArguments().getInt(extraRowId, -1)
        viewModel.appId = requireArguments().getString(extraAppId) ?: ""

        setupToolbar()
        binding.showChangelogList()
        binding.background.isInvisible = true
        binding.appbar.addOnOffsetChangedListener(this)

        binding.retryButton.setOnClickListener {
            binding.error.isVisible = false
            binding.list.isInvisible = true
            binding.retryButton.postDelayed({
                try {
                    viewModel.loadLocalChangelog()
                } catch (e: Exception) {
                    AppLog.e("retryButton", e)
                }
            }, 500)
        }

        if (viewModel.appId.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.cannot_load_app, viewModel.appId), Toast.LENGTH_LONG).show()
            AppLog.e("Cannot loadChangelog app details: '${viewModel.appId}'")
            binding.error.isVisible = true
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.appLoading.collect { loadingState ->
                    when (loadingState) {
                        is Loaded -> {
                        }
                        is NotFound -> {
                            val appId = viewModel.appId
                            Toast.makeText(requireContext(), getString(R.string.cannot_load_app, appId), Toast.LENGTH_LONG).show()
                            AppLog.e("Cannot loadChangelog app details: '${appId}'")
                            binding.showErrorWithRetry()
                        }
                    }
                }
            }

            launch {
                viewModel.app.filterNotNull().collect { app ->
                    if (!loaded) {
                        loaded = true
                        setupAppView(app)
                        binding.list.layoutManager = LinearLayoutManager(requireContext())
                        binding.list.adapter = adapter
                    }
                    val isWatched = app.status != AppInfoMetadata.STATUS_DELETED
                    toggleMenu?.isChecked = isWatched
                    if (!isWatched) {
                        toggleMenu?.isEnabled = true
                    }
                }
            }

            launch {
                viewModel.changelogState.collect {
                    when (it) {
                        is Complete -> {
                            toggleMenu?.isEnabled = true
                            viewModel.app.value?.let { app ->
                                appDetailsView.title.text = app.generateTitle(resources)
                            }
                            adapter.setData(viewModel.localChangelog, viewModel.recentChange)
                            if (adapter.isEmpty) {
                                binding.showErrorWithRetry()
                                if (!provide.networkConnection.isNetworkAvailable) {
                                    Toast.makeText(requireContext(), R.string.check_connection, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                binding.showChangelogList()
                            }
                        }
                        else -> {
                            binding.showChangelogList()
                            adapter.setData(viewModel.localChangelog, viewModel.recentChange)
                        }
                    }
                }
            }

            viewModel.watchStateChange.collect { result ->
                when (result) {
                    AppListTable.ERROR_ALREADY_ADDED -> Toast.makeText(requireContext(), R.string.app_already_added, Toast.LENGTH_SHORT).show()
                    AppListTable.ERROR_INSERT -> Toast.makeText(requireContext(), R.string.error_insert_app, Toast.LENGTH_SHORT).show()
                    else -> {
                        val info = AppInfo(viewModel.document!!)
                        TagSnackbar.make(requireActivity(), info, false).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.error.isVisible) {
            binding.showChangelogList()
        }

        try {
            viewModel.changelogState.tryEmit(Initial)
            viewModel.loadLocalChangelog()
        } catch (e: Exception) {
            AppLog.e("onResume", e)
        }
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
                } catch (e: Exception) {
                    AppLog.e("onResume", e)
                }
            }
        }
    }

    private fun setupAppView(app: App) {
        binding.playStoreButton.setOnClickListener(this)

        appDetailsView.fillDetails(app, false, "", false, app.rowId == -1)
        binding.toolbar.title = titleString

        if (app.iconUrl.isEmpty()) {
            setDefaultIcon()
        } else {
            loadIcon(app.iconUrl)
        }
    }

    private fun loadIcon(imageUrl: String) {
        lifecycleScope.launchWhenCreated {
            try {
                val bitmap = iconLoader.get(imageUrl)
                if (bitmap == null) {
                    setDefaultIcon()
                    return@launchWhenCreated
                }
                val palette = withContext(Dispatchers.Default) { Palette.from(bitmap).generate() }
                binding.header.icon.setImageBitmap(bitmap)
                binding.toolbar.logo = BitmapDrawable(resources, bitmap)
                binding.toolbar.logo.alpha = 0
                onPaletteGenerated(palette)
            } catch (e: Exception) {
                AppLog.e("loadIcon", e)
                setDefaultIcon()
            }
        }
    }

    private fun setDefaultIcon() {
        if (isAdded) {
            binding.background.isVisible = true
            applyColor(ContextCompat.getColor(requireContext(), R.color.theme_accent))
            binding.header.icon.setImageResource(R.drawable.ic_app_icon_placeholder)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.changelog)
        val menu = binding.toolbar.menu
        toggleMenu = menu.findItem(R.id.menu_watch_toggle).wrapCheckStateIcon()
        toggleMenu?.isEnabled = false
        val tagMenu = menu.findItem(R.id.menu_tag_app)
        subscribeForTagSubmenu(tagMenu)
        if (!dataProvider.installedApps.packageInfo(viewModel.appId).isInstalled) {
            menu.findItem(R.id.menu_uninstall).isVisible = false
            menu.findItem(R.id.menu_open).isVisible = false
            menu.findItem(R.id.menu_app_info).isVisible = false
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_white_24)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener(this)
    }

    private fun subscribeForTagSubmenu(tagMenu: MenuItem) {
        tagMenu.isVisible = false
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tagsMenuItems.collectLatest { result ->
                tagMenu.isVisible = result.isNotEmpty()
                val tagSubMenu = tagMenu.subMenu
                tagSubMenu.removeGroup(R.id.menu_group_tags)
                for (item in result) {
                    tagSubMenu.add(R.id.menu_group_tags, item.first.id, 0, item.first.name).isChecked = item.second
                }
                tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_watch_toggle -> {
                if (item.isChecked) {
                    val removeDialog = RemoveDialogFragment.newInstance(
                            viewModel.app.value!!.title, viewModel.app.value!!.rowId
                    )
                    removeDialog.show(childFragmentManager, "removeDialog")
                } else {
                    viewModel.watch()
                }
                return true
            }
            R.id.menu_add -> {
                return true
            }
            R.id.menu_uninstall -> {
                val uninstallIntent = Intent().forUninstall(viewModel.appId)
                startActivity(uninstallIntent)
                return true
            }
            R.id.menu_share -> {
                shareApp()
                return true
            }
            R.id.menu_open -> {
                val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(viewModel.appId)
                if (launchIntent != null) {
                    launchIntent.addMultiWindowFlags(requireContext())
                    startActivitySafely(launchIntent)
                }
            }
            R.id.menu_app_info -> {
                startActivity(Intent().forAppInfo(viewModel.appId, requireContext()))
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

    private fun onPaletteGenerated(palette: Palette?) {
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
        val drawable = DrawableCompat.wrap(binding.playStoreButton.drawable)
        DrawableCompat.setTint(drawable, color)
        binding.playStoreButton.setImageDrawable(drawable)
        binding.background.setBackgroundColor(color)
        binding.retryButton.setBackgroundColor(color)
    }

    private fun animateBackground() {
        binding.background.post {
            if (isVisible) {
                val location = IntArray(2)
                binding.header.icon.getLocationOnScreen(location)
                if (ViewCompat.isAttachedToWindow(binding.background)) {
                    RevealAnimatorCompat.show(binding.background, location[0], location[1], 0).start()
                }
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.playStoreButton) {
            val intent = Intent().forPlayStore(viewModel.app.value!!.packageName, requireContext())
            this.startActivitySafely(intent)
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
        val alpha = 1.0f - abs(verticalOffset.toFloat() / totalScrollRange)

        if (!loaded) {
            return
        }

        binding.header.root.alpha = alpha
        binding.playStoreButton.alpha = alpha
        binding.playStoreButton.isEnabled = alpha > 0.8f

        val inverseAlpha = (1.0f - alpha)
        binding.toolbar.logo?.alpha = (inverseAlpha * 255).toInt()
        titleString.alpha = inverseAlpha
        subtitleString.alpha = inverseAlpha
        binding.container.post {
            _binding?.toolbar?.let {
                it.title = titleString
                it.subtitle = subtitleString
                _binding?.playStoreButton?.translationY = verticalOffset.toFloat()
            }
        }
    }

    companion object {
        const val extraAppId = "app_id"
        const val extraDetailsUrl = "url"
        const val extraRowId = "row_id"

        const val tag = "DetailsFragment"
        fun newInstance(appId: String, detailsUrl: String, rowId: Int) = DetailsFragment().apply {
            arguments = Bundle().apply {
                putString(extraAppId, appId)
                putString(extraDetailsUrl, detailsUrl)
                putInt(extraRowId, rowId)
            }
        }
    }

}

fun FragmentAppChangelogBinding.showErrorWithRetry() {
    error.isVisible = true
    list.isInvisible = true
}

fun FragmentAppChangelogBinding.showChangelogList() {
    error.isVisible = false
    list.isVisible = true
}