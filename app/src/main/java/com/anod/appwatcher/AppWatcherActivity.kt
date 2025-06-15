package com.anod.appwatcher

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.EmptyBoxSmile
import com.anod.appwatcher.watchlist.MainScreenScene
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import info.anodsplace.framework.content.onCommonActivityAction
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import androidx.core.net.toUri

@Serializable
data object MainScreenNavKey : NavKey

@Serializable
data class SelectedAppNavKey(val selectedApp: App) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AppWatcherActivity : BaseComposeActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val backStack = rememberNavBackStack(MainScreenNavKey)
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
            NavDisplay(
                backStack = backStack,
                onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                sceneStrategy = listDetailStrategy,
                entryProvider = entryProvider {
                    entry<MainScreenNavKey>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                EmptyBoxSmile()
                            }
                        )
                    ) {
                        val wideLayout by foldableDevice.layout.collectAsState()
                        MainScreenScene(
                            prefs = prefs,
                            wideLayout = wideLayout
                        )
                    }
                    entry<SelectedAppNavKey>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { key ->
                            DetailContent(
                                app = key.selectedApp,
                                onDismissRequest = {  },
                                onCommonActivityAction = { onCommonActivityAction(it) }
                            )
                    }
                }
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