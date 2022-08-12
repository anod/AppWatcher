package com.anod.appwatcher.tags

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices.FOLDABLE
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.details.DetailsScreen
import com.anod.appwatcher.installed.InstalledFragment
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListSharedStateAction
import com.anod.appwatcher.watchlist.WatchListSharedStateEvent
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import com.google.android.material.color.DynamicColors
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.*
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

    private lateinit var hingeDevice: HingeDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = ApplicationContext(this)
        AppCompatDelegate.setDefaultNightMode(app.appCompatNightMode)
        hingeDevice = HingeDevice.create(this)
        super.onCreate(savedInstanceState)
        setTheme(this.themeRes)
        DynamicColors.applyToActivityIfAvailable(this)

        viewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(hingeDevice.layout.value))

        setContent {
            val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

            AppTheme(
                    customPrimaryColor = Color(screenState.tag.color),
                    theme = viewModel.prefs.theme
            ) {
                val pagingSourceConfig = WatchListPagingSource.Config(
                        filterId = screenState.filterId,
                        tagId = if (screenState.tag.isEmpty) null else screenState.tag.id,
                        showRecentlyUpdated = viewModel.prefs.showRecentlyUpdated,
                        showOnDevice = false,
                        showRecentlyInstalled = false
                )

                if (screenState.wideLayout.isWideLayout) {
                    MainDetailsScreen(
                            wideLayout = screenState.wideLayout,
                            main = {
                                TagWatchListScreen(screenState = screenState, pagingSourceConfig = pagingSourceConfig, onEvent = { viewModel.handleEvent(it) })
                            },
                            detail = {
                                DetailContent(app = screenState.selectedApp)
                            }
                    )
                } else {
                    TagWatchListScreen(screenState = screenState, pagingSourceConfig = pagingSourceConfig, onEvent = { viewModel.handleEvent(it) })
                }
            }
        }

        lifecycleScope.launch {
            viewModel.viewActions.collect { onViewAction(it) }
        }

        lifecycleScope.launchWhenCreated {
            hingeDevice.layout.collect {
                viewModel.handleEvent(WatchListSharedStateEvent.SetWideLayout(it))
            }
        }
    }

    private fun onViewAction(action: WatchListSharedStateAction) {
        when (action) {
            WatchListSharedStateAction.OnBackPressed -> onBackPressed()
            is WatchListSharedStateAction.OpenApp -> {
                val app = action.app
                if (BuildConfig.DEBUG) {
                    AppLog.d(app.packageName)
                }
                DetailsDialog.show(app.appId, app.rowId, app.detailsUrl, supportFragmentManager)
            }
            is WatchListSharedStateAction.ExpandSection -> {}
            is WatchListSharedStateAction.SearchInStore -> startActivity(MarketSearchActivity.intent(this, "", true))
            is WatchListSharedStateAction.ImportInstalled -> Theme(this, viewModel.prefs).also { theme -> startActivity(InstalledFragment.intent(true, this, theme.theme, theme.colors)) }
            is WatchListSharedStateAction.ShareFromStore -> startActivitySafely(Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity")))
            is WatchListSharedStateAction.AddAppToTag -> AppsTagSelectDialog.show(action.tag, supportFragmentManager)
            is WatchListSharedStateAction.EditTag -> EditTagDialog.show(supportFragmentManager, tag = action.tag, theme = Theme(this, viewModel.prefs))
            is WatchListSharedStateAction.OnSearch -> startActivity(MarketSearchActivity.intent(this, action.query, true))
        }
    }

    override fun onBackPressed() {
        if (viewModel.viewState.wideLayout.isWideLayout) {
            if (viewModel.viewState.selectedApp != null) {
                viewModel.handleEvent(WatchListSharedStateEvent.SelectApp(app = null))
            } else {
                super.onBackPressed()
            }
        } else if (!DetailsDialog.dismiss(supportFragmentManager)) {
            super.onBackPressed()
        }
    }

    companion object {
        fun createTagIntent(tag: Tag, context: Context) = Intent(context, TagWatchListComposeActivity::class.java).apply {
            putExtra(AppsTagViewModel.EXTRA_TAG, tag)
            addMultiWindowFlags(context)
        }
    }
}


@Composable
fun MainDetailsScreen(
        wideLayout: HingeDeviceLayout,
        main: @Composable () -> Unit,
        detail: @Composable () -> Unit,
) {
    Row(
            modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()) {
            main()
        }
        val hingeWidth = wideLayout.hinge.width()
        if (hingeWidth > 0) {
            val widthInDp = with(LocalDensity.current) { hingeWidth.toDp() }
            Spacer(modifier = Modifier
                    .width(widthInDp)
                    .fillMaxHeight())
        }
        Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()) {
            detail()
        }
    }
}

@Composable
fun DetailContent(app: App?) {
    Surface {
        if (app == null) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = R.drawable.ic_empty_box_smile), contentDescription = null)
            }
        } else {
            DetailsScreen(appId = app.appId, rowId = app.rowId, detailsUrl = app.detailsUrl ?: "")
        }
    }
}

@Preview(device = FOLDABLE)
@Composable
fun MainDetailsScreenPreview() {
    AppTheme {
        MainDetailsScreen(
                wideLayout = HingeDeviceLayout(isWideLayout = true, hinge = Rect(0, 0, 80, 0)),
                main = { DetailContent(app = null) },
                detail = { DetailContent(app = null) },
        )
    }

}