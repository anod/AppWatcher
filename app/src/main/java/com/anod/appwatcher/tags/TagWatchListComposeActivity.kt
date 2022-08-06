package com.anod.appwatcher.tags

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.details.DetailsFragment
import com.anod.appwatcher.installed.InstalledFragment
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.WatchListAction
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListSharedStateAction
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import com.google.android.material.color.DynamicColors
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.addMultiWindowFlags
import info.anodsplace.framework.content.startActivitySafely
import kotlinx.coroutines.launch

class TagWatchListComposeActivity : AppCompatActivity() {
    val viewModel: WatchListStateViewModel by viewModels()

    val theme: Theme
        get() = Theme(this, viewModel.prefs)

    private val themeRes: Int
        get() = if (themeColors.statusBarColor.isLight)
            theme.themeLightActionBar
        else
            theme.themeDarkActionBar

    private val themeColors: CustomThemeColors
        get() = CustomThemeColors(viewModel.viewState.tag.color, theme.colors.navigationBarColor)

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.appCompatNightMode)
        super.onCreate(savedInstanceState)
        setTheme(this.themeRes)
        DynamicColors.applyToActivityIfAvailable(this)

        lifecycleScope.launch {
            viewModel.viewActions.collect { onViewAction(it) }
        }

        setContent {
            AppTheme(
                    customPrimaryColor = Color(viewModel.viewState.tag.color)
            ) {
                val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
                val pagingSourceConfig = WatchListPagingSource.Config(
                        filterId = screenState.filterId,
                        tag = screenState.tag,
                        showRecentlyUpdated = viewModel.prefs.showRecentlyUpdated,
                        showOnDevice = false,
                        showRecentlyInstalled = false
                )
                TagWatchListScreen(screenState = screenState, pagingSourceConfig = pagingSourceConfig, onEvent = { viewModel.handleEvent(it) })
            }
        }
    }

    private fun onViewAction(action: WatchListSharedStateAction) {
        when (action) {
            WatchListSharedStateAction.OnBackPressed -> onBackPressed()
            is WatchListSharedStateAction.ListAction -> {
                when (val listAction = action.action) {
                    is WatchListAction.SearchInStore -> startActivity(MarketSearchActivity.intent(this, "", true))
                    is WatchListAction.Installed -> {
                        val theme = Theme(this, viewModel.prefs)
                        startActivity(InstalledFragment.intent(listAction.importMode, this, theme.theme, theme.colors))
                    }
                    is WatchListAction.ShareFromStore -> startActivitySafely(Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")))
                    is WatchListAction.ItemClick -> {
                        val app = listAction.app
                        if (BuildConfig.DEBUG) {
                            AppLog.d(app.packageName)
                        }
                        openAppDetails(app.appId, app.rowId, app.detailsUrl)
                    }
                    else -> {}
                }
            }
            is WatchListSharedStateAction.AddAppToTag -> AppsTagSelectDialog.show(action.tag, supportFragmentManager)
            is WatchListSharedStateAction.EditTag -> EditTagDialog.show(supportFragmentManager, tag = action.tag, theme = Theme(this, viewModel.prefs))
        }
    }

    private fun openAppDetails(appId: String, rowId: Int, detailsUrl: String?) {
        if (viewModel.viewState.isWideLayout) {
            supportFragmentManager.commit {
                add(R.id.details, DetailsFragment.newInstance(appId, detailsUrl ?: "", rowId), DetailsFragment.tag)
                addToBackStack(DetailsFragment.tag)
            }
        } else {
            DetailsDialog.show(appId, rowId, detailsUrl, supportFragmentManager)
        }
    }

    override fun onBackPressed() {
        if (viewModel.viewState.isWideLayout && supportFragmentManager.findFragmentByTag(DetailsFragment.tag) != null) {
            supportFragmentManager.popBackStack(DetailsFragment.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            if (!DetailsDialog.dismiss(supportFragmentManager)) {
                super.onBackPressed()
            }
        }
    }

    companion object {
        fun createTagIntent(tag: Tag, context: Context) = Intent(context, TagWatchListComposeActivity::class.java).apply {
            putExtra(AppsTagViewModel.EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}