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
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.installed.InstalledListScreenScene
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.preferences.SettingsScreenScene
import com.anod.appwatcher.search.SearchResultsScreenScene
import com.anod.appwatcher.tags.TagWatchListScreenScene
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.EmptyBoxSmile
import com.anod.appwatcher.watchlist.MainScreenScene
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AppWatcherActivity : BaseComposeActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)

        val elements = createInitialBackstack()
        setContent {

            val backStack = rememberNavBackStack(*elements)
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

    private fun createInitialBackstack(): Array<NavKey> {
        val extras = intent?.extras ?: bundleOf()
        var elements = arrayOf<NavKey>(SceneNavKey.Main)
        if (extras.containsKey("open_recently_installed")) {
            intent!!.extras!!.remove("open_recently_installed")
            elements += SceneNavKey.Installed(importMode = false)
        } else if (extras.containsKey(WatchListStateViewModel.EXTRA_TAG_ID)) {
            val extraTagId = extras.getInt(WatchListStateViewModel.EXTRA_TAG_ID)
            intent!!.extras!!.remove(WatchListStateViewModel.EXTRA_TAG_ID)
            elements += SceneNavKey.TagWatchList(
                tag = Tag(
                    id = extraTagId,
                    name = "",
                    color = extras.getInt(WatchListStateViewModel.EXTRA_TAG_COLOR)
                )
            )
        }
        return elements
    }

    private fun provideNavEntries(backStack: NavBackStack): (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<SceneNavKey.Main>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = SceneNavKey.Main,
                detailPlaceholder = {
                    EmptyBoxSmile()
                }
            )
        ) {
            val wideLayout by foldableDevice.layout.collectAsState()
            MainScreenScene(
                prefs = prefs,
                wideLayout = wideLayout,
                navigateBack = { backStack.removeLastOrNull() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.AppDetails>(
            metadata = ListDetailSceneStrategy.detailPane(sceneKey = SceneNavKey.AppDetails)
        ) { key ->
            DetailContent(
                app = key.selectedApp,
                onDismissRequest = { backStack.removeLastOrNull() },
            )
        }
        entry<SceneNavKey.Search>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = SceneNavKey.Search,
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
        entry<SceneNavKey.Settings>(
            metadata = ListDetailSceneStrategy.extraPane(sceneKey = SceneNavKey.Settings)
        ) {
            SettingsScreenScene(
                navigateBack = { backStack.removeLastOrNull() }
            )
        }
        entry<SceneNavKey.TagWatchList>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = SceneNavKey.TagWatchList,
                detailPlaceholder = {
                    EmptyBoxSmile()
                }
            )
        ) { key ->
            val wideLayout by foldableDevice.layout.collectAsState()
            TagWatchListScreenScene(
                tag = key.tag,
                wideLayout = wideLayout,
                navigateBack = { backStack.removeLastOrNull() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.Installed>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = SceneNavKey.Installed,
                detailPlaceholder = {
                    EmptyBoxSmile()
                }
            )
        ) { key ->
            InstalledListScreenScene(
                showAction = key.importMode,
                navigateBack = { backStack.removeLastOrNull() },
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