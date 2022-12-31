// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.details

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
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
import com.anod.appwatcher.R
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.databinding.FragmentAppChangelogBinding
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.*
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.ColorRoles
import com.google.android.material.color.MaterialColors
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.anim.RevealAnimatorCompat
import info.anodsplace.framework.app.addMultiWindowFlags
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.graphics.chooseDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class DetailsFragment : Fragment(), View.OnClickListener, AppBarLayout.OnOffsetChangedListener, Toolbar.OnMenuItemClickListener {

    private val viewModel: DetailsViewModel by viewModels(factoryProducer = {
        DetailsViewModel.Factory(
                argAppId = requireArguments().getString(extraAppId) ?: "",
                argRowId = requireArguments().getInt(extraRowId, -1),
                argDetailsUrl = requireArguments().getString(extraDetailsUrl) ?: ""
        )
    })
    private var toggleMenu: MenuItem? = null
    private var tagMenu: MenuItem? = null

    private val titleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(Color.WHITE)
        AlphaSpannableString(viewModel.viewState.app!!.generateTitle(resources), span)
    }

    private val subtitleString: AlphaSpannableString by lazy {
        val span = AlphaForegroundColorSpan(Color.WHITE)
        AlphaSpannableString(viewModel.viewState.app!!.uploadDate, span)
    }

    private val dataProvider: AppViewHolderResourceProvider by lazy {
        AppViewHolderResourceProvider(requireContext(), viewModel.installedApps)
    }

    private val appDetailsView: AppDetailsView by lazy {
        AppDetailsView(
                binding.container,
                dataProvider
        )
    }

    private val adapter: ChangesAdapter by lazy { ChangesAdapter(requireContext()) }

    private var _binding: FragmentAppChangelogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppChangelogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        binding.showChangelogList()
        binding.background.isInvisible = true
        binding.appbar.addOnOffsetChangedListener(this)

        binding.retryButton.setOnClickListener {
            binding.error.isVisible = false
            binding.list.isInvisible = true
            binding.retryButton.postDelayed({
                viewModel.handleEvent(DetailsScreenEvent.ReloadChangelog)
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
                viewModel.viewStates.map { it.appIconState }.distinctUntilChanged().collect { appIconState ->
                    AppLog.d("Details collecting appIconState $appIconState")
                    when (appIconState) {
                        is AppIconState.Loaded -> {
                            try {
                                val bitmap = appIconState.drawable.bitmap
                                binding.header.icon.setImageBitmap(bitmap)
                                binding.toolbar.logo = appIconState.drawable
                                binding.toolbar.logo.alpha = 0
                                val palette = withContext(Dispatchers.Default) { Palette.from(bitmap).generate() }
                                onPaletteGenerated(palette)
                            } catch (e: Exception) {
                                AppLog.e("loadIcon", e)
                                setDefaultIcon()
                            }
                        }
                        AppIconState.Default -> {
                            setDefaultIcon()
                        }
                        AppIconState.Initial -> {}
                    }
                }
            }

            launch {
                viewModel.viewStates.filter { it.appLoadingState is AppLoadingState.NotFound }.distinctUntilChanged().collect {
                    AppLog.d("Details collecting appLoadingState AppLoadingState.NotFound")
                    if (!viewModel.viewState.errorShown) {
                        viewModel.errorShown = true
                        val appId = viewModel.appId
                        Toast.makeText(requireContext(), getString(R.string.cannot_load_app, appId), Toast.LENGTH_LONG).show()
                        AppLog.e("Cannot loadChangelog app details: '${appId}'")
                        binding.showErrorWithRetry()
                    }
                }
            }

            launch {
                viewModel.viewStates
                        .map { it.appLoadingState is AppLoadingState.Loaded }
                        .filter { it }
                        .distinctUntilChanged().collect { loaded ->
                            val viewState = viewModel.viewState
                            AppLog.d("Details collecting loaded view state $loaded $viewState")
                            updateAppView(viewState.app!!, viewState.appIconState)

                            val isWatched = viewState.app.status != AppInfoMetadata.STATUS_DELETED
                            toggleMenu?.isChecked = isWatched
                            if (!isWatched) {
                                toggleMenu?.isEnabled = true
                            }
                        }
            }

            launch {
                viewModel.viewStates.map { it.tagsMenuItems }.distinctUntilChanged().collect { tagsMenuItems ->
                    AppLog.d("Details collecting tagsMenuItems $tagsMenuItems")
                    tagMenu?.isVisible = tagsMenuItems.isNotEmpty()
                    tagMenu?.subMenu?.also { tagSubMenu ->
                        tagSubMenu.removeGroup(R.id.menu_group_tags)
                        for (item in tagsMenuItems) {
                            tagSubMenu.add(R.id.menu_group_tags, item.first.id, 0, item.first.name).isChecked = item.second
                        }
                        tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false)
                    }
                }
            }

            launch {
                viewModel.viewStates.map { it.changelogState }.distinctUntilChanged().filter { it !is ChangelogLoadState.Initial }.collect { changelogState ->
                    AppLog.d("Details collecting changelogState $changelogState")
                    val viewState = viewModel.viewState
                    when (changelogState) {
                        is ChangelogLoadState.Complete -> {
                            toggleMenu?.isEnabled = true
                            adapter.setData(viewState.localChangelog, viewState.recentChange, viewState.accentColorRoles?.accent)
                            if (adapter.isEmpty) {
                                binding.showErrorWithRetry()
                                if (!viewModel.networkConnection.isNetworkAvailable) {
                                    Toast.makeText(requireContext(), R.string.check_connection, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                binding.showChangelogList()
                            }
                        }
                        else -> {
                            binding.showChangelogList()
                            adapter.setData(viewState.localChangelog, viewState.recentChange, viewState.accentColorRoles?.accent)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewActions.collect { action ->
                when (action) {
                    is DetailsScreenAction.WatchAppResult -> when (action.result) {
                        AppListTable.ERROR_ALREADY_ADDED -> Toast.makeText(requireContext(), R.string.app_already_added, Toast.LENGTH_SHORT).show()
                        AppListTable.ERROR_INSERT -> Toast.makeText(requireContext(), R.string.error_insert_app, Toast.LENGTH_SHORT).show()
                        else -> {
                            val info = AppInfo(viewModel.viewState.document!!, viewModel.uploadDateParserCache)
                            TagSnackbar.make(view, info, false, requireActivity(), viewModel.prefs).show()
                        }
                    }
                    is DetailsScreenAction.AuthTokenStartIntent -> {
                        startActivitySafely(action.intent)
                    }
                }
            }

        }

        viewModel.observeApp()
    }

    override fun onResume() {
        super.onResume()
        if (binding.error.isVisible) {
            binding.showChangelogList()
        }

        viewModel.handleEvent(DetailsScreenEvent.LoadChangelog)
    }


    private fun updateAppView(app: App, appIconState: AppIconState) {
        binding.playStoreButton.setOnClickListener(this)

        appDetailsView.fillDetails(app, app.rowId == -1)
        binding.toolbar.title = titleString
        if (appDetailsView.creator?.text?.isNotEmpty() == true) {
            appDetailsView.creator?.isVisible = true
        }

        if (appIconState is AppIconState.Default) {
            setDefaultIcon()
        }
    }

    private fun setDefaultIcon() {
        if (isAdded) {
            binding.background.isVisible = true
            applyColor(ContextCompat.getColor(requireContext(), R.color.md_primary))
            binding.header.icon.setImageResource(R.drawable.ic_app_icon_placeholder)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.changelog)
        val menu = binding.toolbar.menu
        toggleMenu = menu.findItem(R.id.menu_watch_toggle).wrapCheckStateIcon()
        toggleMenu?.isEnabled = false
        tagMenu = menu.findItem(R.id.menu_tag_app)
        tagMenu?.isVisible = false
        if (!viewModel.viewState.isInstalled) {
            menu.findItem(R.id.menu_uninstall).isVisible = false
            menu.findItem(R.id.menu_open).isVisible = false
            menu.findItem(R.id.menu_app_info).isVisible = false
        }

        binding.toolbar.overflowIcon?.setTint(Color.WHITE)

        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_white_24)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_watch_toggle -> {
                if (item.isChecked) {
                    val removeDialog = RemoveDialogFragment.newInstance(
                            viewModel.viewState.app!!.title, viewModel.viewState.app!!.rowId
                    )
                    removeDialog.show(childFragmentManager, "removeDialog")
                } else {
                    viewModel.handleEvent(DetailsScreenEvent.WatchApp)
                }
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
            viewModel.handleEvent(DetailsScreenEvent.UpdateTag(item.itemId, item.isChecked))
        }

        return false
    }

    private fun shareApp() {
        val appInfo = viewModel.viewState.app ?: return
        val builder = ShareCompat.IntentBuilder(requireActivity())

        val changes = if (viewModel.viewState.recentChange.details.isBlank()) "" else "${viewModel.viewState.recentChange.details}\n\n"
        val text = getString(R.string.share_text, changes, String.format(StoreIntent.URL_WEB_PLAY_STORE, appInfo.packageName))

        builder.setSubject(getString(R.string.share_subject, appInfo.title, appInfo.versionName))
        builder.setText(text)
        builder.setType("text/plain")
        builder.startChooser()
    }

    private fun onPaletteGenerated(palette: Palette?) {
        val context = context ?: return
        val defaultColor = ContextCompat.getColor(context, R.color.md_surface)
        val darkSwatch = palette?.chooseDark(defaultColor) ?: Palette.Swatch(defaultColor, 0)
        val colorRoles: ColorRoles = MaterialColors.getColorRoles(darkSwatch.rgb, false)

        viewModel.handleEvent(DetailsScreenEvent.UpdateAccentColor(colorRoles))

        applyColor(colorRoles.accentContainer)
        animateBackground()

        if (Theme(context, prefs = viewModel.prefs).isNightMode) {
            appDetailsView.updateAccentColor(colorRoles.onAccentContainer)
        } else {
            appDetailsView.updateAccentColor(colorRoles.accentContainer)
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
                    RevealAnimatorCompat.show(binding.background, location[0], location[1], 0).also { animator ->
                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                _binding?.toolbar?.background = _binding?.background?.background?.constantState?.newDrawable()
                                animator.removeAllListeners()
                            }
                        })
                        animator.start()
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.playStoreButton) {
            val intent = Intent().forPlayStore(viewModel.viewState.app!!.packageName, requireContext())
            this.startActivitySafely(intent)
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
        val alpha = 1.0f - abs(verticalOffset.toFloat() / totalScrollRange)

        if (viewModel.viewState.appLoadingState !is AppLoadingState.Loaded) {
            return
        }

        binding.header.root.alpha = alpha
        binding.playStoreButton.alpha = alpha
        binding.playStoreButton.isEnabled = alpha > 0.8f

        val inverseAlpha = (1.0f - alpha)
        val inverseAlphaInt = (inverseAlpha * 255).toInt()
        binding.toolbar.logo?.alpha = inverseAlphaInt
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