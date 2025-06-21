package com.anod.appwatcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.EmptyBoxSmile
import com.anod.appwatcher.watchlist.MainScreenScene
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import org.koin.core.component.KoinComponent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.navigation.MainScreenNavKey
import com.anod.appwatcher.navigation.MarketSearchNavKey
import com.anod.appwatcher.navigation.SelectedAppNavKey
import com.anod.appwatcher.navigation.SettingsNavKey
import com.anod.appwatcher.preferences.SettingsScreenScene
import com.anod.appwatcher.search.SearchResultsScreenScene

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AppWatcherActivity : BaseComposeActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)

        setContent {
            val backStack = rememberNavBackStack(MainScreenNavKey)
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
            AppTheme(
                theme = prefs.theme,
                transparentSystemUi = true
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                    sceneStrategy = listDetailStrategy,
                    entryProvider = provideNavEntries(backStack)
                )
            }
        }
    }

    private fun provideNavEntries(backStack: NavBackStack): (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<MainScreenNavKey>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = MainScreenNavKey,
                detailPlaceholder = {
                    EmptyBoxSmile()
                }
            )
        ) {
            val wideLayout by foldableDevice.layout.collectAsState()
            MainScreenScene(
                prefs = prefs,
                wideLayout = wideLayout,
                backStack = backStack,
            )
        }
        entry<SelectedAppNavKey>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) { key ->
            DetailContent(
                app = key.selectedApp,
                onDismissRequest = { backStack.removeLastOrNull() },
            )
        }
        entry<MarketSearchNavKey>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = MarketSearchNavKey,
                detailPlaceholder = {
                    EmptyBoxSmile()
                }
            )
        ) {
            val wideLayout by foldableDevice.layout.collectAsState()
            SearchResultsScreenScene(
                wideLayout = wideLayout,
                navigateBack = { backStack.removeLastOrNull() },
            )
        }
        entry<SettingsNavKey> {
            SettingsScreenScene(
                navigateBack = { backStack.removeLastOrNull() }
            )
        }
    }

    companion object {
        fun createTagShortcutIntent(tagId: Int, initialColor: Int, context: Context) = Intent(context, AppWatcherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = "com.anod.appwatcher://tags/$tagId?color=$initialColor".toUri()
            putExtra(WatchListStateViewModel.EXTRA_TAG_ID, tagId)
            putExtra(WatchListStateViewModel.EXTRA_TAG_COLOR, initialColor)
        }
    }
}