package com.anod.appwatcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.history.HistoryListScreenScene
import com.anod.appwatcher.installed.InstalledListScreenScene
import com.anod.appwatcher.navigation.ResizableListDetailSceneStrategy
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.navigation.rememberResizableListDetailSceneStrategy
import com.anod.appwatcher.preferences.SettingsScreenScene
import com.anod.appwatcher.search.SearchResultsScreenScene
import com.anod.appwatcher.search.toViewState
import com.anod.appwatcher.sync.SchedulesHistoryScreenScene
import com.anod.appwatcher.tags.TagWatchListScreenScene
import com.anod.appwatcher.userLog.UserLogScreenScene
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.DetailContent
import com.anod.appwatcher.watchlist.DetailPlaceholder
import com.anod.appwatcher.watchlist.MainScreenScene
import com.anod.appwatcher.wishlist.WishListScreenScene
import info.anodsplace.framework.app.addMultiWindowFlags
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AppWatcherActivity : BaseComposeActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)

        val elements = createInitialBackstack()
        setContent {
            val backStack = rememberNavBackStack(*elements)
            val navigateBack = { backStack.navigateBackOrFinish() }
            val listDetailStrategy = rememberResizableListDetailSceneStrategy<NavKey>(
                sceneContainer = { content ->
                    AppTheme(
                        theme = prefs.selectedTheme,
                        updateSystemBars = false
                    ) {
                        content()
                    }
                },
                paneExpansionDragHandle = { paneExpansionState, modifier ->
                    val interactionSource = remember { MutableInteractionSource() }
                    VerticalDragHandle(
                        modifier = modifier
                            .paneExpansionDraggable(
                                state = paneExpansionState,
                                minTouchTargetSize = 48.dp,
                                interactionSource = interactionSource,
                            ),
                        interactionSource = interactionSource,
                    )
                }
            )
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                onBack = navigateBack,
                sceneStrategies = listOf(listDetailStrategy),
                entryProvider = provideNavEntries(backStack),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                transitionSpec = {
                    // Slide in from right when navigating forward
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(350)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(350)
                    )
                },
                popTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(350)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350)
                    )
                },
                predictivePopTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(350)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350)
                    )
                }
            )
        }
    }

    private fun createInitialBackstack(): Array<NavKey> {
        val extras = intent?.extras ?: bundleOf()
        if (extras.containsKey(EXTRA_SEARCH_KEYWORD)) {
            intent!!.extras!!.remove(EXTRA_SEARCH_KEYWORD)
            return arrayOf(SceneNavKey.Search(
                keyword = intent?.getStringExtra(EXTRA_SEARCH_KEYWORD) ?: "",
                focus = intent?.getBooleanExtra(EXTRA_SEARCH_FOCUS, false) ?: false,
                initiateSearch = intent?.getBooleanExtra(EXTRA_SEARCH_EXACT, false) ?: false,
                isPackageSearch = intent?.getBooleanExtra(EXTRA_SEARCH_PACKAGE, false) ?: false,
                isShareSource = intent?.getBooleanExtra(EXTRA_SEARCH_SHARE, false) ?: false,
            ))
        }
        if (extras.containsKey(EXTRA_LIST_TAG_ID)) {
            val extraTagId = extras.getInt(EXTRA_LIST_TAG_ID)
            val extraTagColor = extras.getInt(EXTRA_LIST_TAG_COLOR, Tag.DEFAULT_COLOR)
            // intent!!.extras!!.remove(EXTRA_DETAILS_TAG_ID)
            return arrayOf(SceneNavKey.TagWatchList(
                tag = Tag(
                    id = extraTagId,
                    name = "",
                    color = extraTagColor
                )
            ))
        }
        var elements = arrayOf<NavKey>(SceneNavKey.Main)
        if (extras.containsKey(EXTRA_OPEN_RECENTLY_INSTALLED)) {
            intent!!.extras!!.remove(EXTRA_OPEN_RECENTLY_INSTALLED)
            elements += SceneNavKey.Installed(importMode = false)
        } else if (extras.containsKey(EXTRA_LIST_TAG_ID)) {
            val extraTagId = extras.getInt(EXTRA_LIST_TAG_ID)
            intent!!.extras!!.remove(EXTRA_LIST_TAG_ID)
            elements += SceneNavKey.TagWatchList(
                tag = Tag(
                    id = extraTagId,
                    name = "",
                    color = extras.getInt(EXTRA_LIST_TAG_COLOR)
                )
            )
        } else if (extras.containsKey(EXTRA_GDRIVE_SIGNIN)) {
            intent!!.extras!!.remove(EXTRA_GDRIVE_SIGNIN)
            elements += SceneNavKey.Settings
        }
        return elements
    }

    private fun provideNavEntries(backStack: NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<SceneNavKey.Main>(
            metadata = ResizableListDetailSceneStrategy.listPane(
                sceneKey = "list-detail",
                detailPlaceholder = {
                    DetailPlaceholder(theme = prefs.selectedTheme)
                }
            )
        ) {
            MainScreenScene(
                prefs = prefs,
                navigateBack = { backStack.navigateBackOrFinish() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.AppDetails>(
            metadata = ResizableListDetailSceneStrategy.detailPane(sceneKey = "list-detail")
        ) { key ->
            DetailContent(
                app = key.selectedApp,
                theme = prefs.selectedTheme,
                onDismissRequest = { backStack.navigateBackOrFinish() },
            )
        }
        entry<SceneNavKey.Search>(
            metadata = ResizableListDetailSceneStrategy.listPane(
                sceneKey = "list-detail",
                detailPlaceholder = {
                    DetailPlaceholder(theme = prefs.selectedTheme)
                }
            )
        ) { key ->
            SearchResultsScreenScene(
                initialState = key.toViewState(),
                prefs = prefs,
                navigateBack = { backStack.navigateBackOrFinish() },
            )
        }
        entry<SceneNavKey.Settings> {
            SettingsScreenScene(
                prefs = prefs,
                navigateBack = { backStack.navigateBackOrFinish() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.PurchaseHistory>(
            metadata = ResizableListDetailSceneStrategy.listPane(
                sceneKey = "list-detail",
                detailPlaceholder = {
                    DetailPlaceholder(theme = prefs.selectedTheme)
                }
            )
        ) {
            HistoryListScreenScene(
                prefs = prefs,
                navigateBack = { backStack.navigateBackOrFinish() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.UserLog> {
            UserLogScreenScene(
                prefs = prefs,
                navigateBack = { backStack.navigateBackOrFinish() }
            )
        }
        entry<SceneNavKey.RefreshHistory> {
            SchedulesHistoryScreenScene(
                navigateBack = { backStack.navigateBackOrFinish() }
            )
        }
        entry<SceneNavKey.TagWatchList>(
            metadata = ResizableListDetailSceneStrategy.listPane(
                sceneKey = "list-detail",
                detailPlaceholder = {
                    DetailPlaceholder(theme = prefs.selectedTheme)
                }
            )
        ) { key ->
            TagWatchListScreenScene(
                tag = key.tag,
                navigateBack = { backStack.navigateBackOrFinish() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.Installed>(
            metadata = ResizableListDetailSceneStrategy.listPane(
                sceneKey = "list-detail",
                detailPlaceholder = {
                    DetailPlaceholder(theme = prefs.selectedTheme)
                }
            )
        ) { key ->
            InstalledListScreenScene(
                prefs = prefs,
                showAction = key.importMode,
                navigateBack = { backStack.navigateBackOrFinish() },
                navigateTo = { backStack.add(it) }
            )
        }
        entry<SceneNavKey.WishList>(
            metadata = ResizableListDetailSceneStrategy.listPane(
                sceneKey = "list-detail",
                detailPlaceholder = {
                    DetailPlaceholder(theme = prefs.selectedTheme)
                }
            )
        ) {
            WishListScreenScene(
                prefs = prefs,
                navigateBack = { backStack.navigateBackOrFinish() },
                navigateTo = { backStack.add(it) }
            )
        }
    }

    private fun NavBackStack<NavKey>.navigateBackOrFinish() {
        if (size > 1) {
            removeLastOrNull()
        } else {
            finish()
        }
    }

    companion object {
        const val EXTRA_SEARCH_KEYWORD = "search.keyword"
        const val EXTRA_SEARCH_EXACT = "search.exact"
        const val EXTRA_SEARCH_SHARE = "search.share"
        const val EXTRA_SEARCH_FOCUS = "search.focus"
        const val EXTRA_SEARCH_PACKAGE = "search.package"

        const val EXTRA_LIST_TAG = "extra_tag"
        const val EXTRA_LIST_TAG_ID = "tag_id"
        const val EXTRA_LIST_TAG_COLOR = "tag_color"

        const val EXTRA_FROM_NOTIFICATION = "list.extra_noti"
        const val EXTRA_EXPAND_SEARCH = "list.expand_search"

        const val EXTRA_OPEN_RECENTLY_INSTALLED = "open_recently_installed"
        const val EXTRA_GDRIVE_SIGNIN = "gdrive.signin"

        const val ARG_FILTER = "filter"
        const val ARG_SORT = "sort"
        const val ARG_TAG = "tag"
        const val ARG_SHOW_ACTION = "showAction"

        fun tagShortcutIntent(tagId: Int, initialColor: Int, context: Context) = Intent(context, AppWatcherActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = "com.anod.appwatcher://tags/$tagId?color=$initialColor".toUri()
            putExtra(EXTRA_LIST_TAG_ID, tagId)
            putExtra(EXTRA_LIST_TAG_COLOR, initialColor)
        }

        fun searchIntent(
            context: Context,
            keyword: String,
            focus: Boolean,
            initiateSearch: Boolean = false
        ): Intent = Intent(context, AppWatcherActivity::class.java).apply {
            putExtra(EXTRA_SEARCH_KEYWORD, keyword)
            putExtra(EXTRA_SEARCH_FOCUS, focus)
            putExtra(EXTRA_SEARCH_EXACT, initiateSearch)
        }

        fun tagIntent(tag: Tag, context: Context) = Intent(context, AppWatcherActivity::class.java).apply {
            putExtra(EXTRA_LIST_TAG, tag)
            addMultiWindowFlags(context)
        }

        fun gDriveSignInIntent(context: Context) = Intent(context, AppWatcherActivity::class.java).apply {
            putExtra(EXTRA_GDRIVE_SIGNIN, true)
            addMultiWindowFlags(context)
        }

        private fun installedIntent(sortId: Int, showImportAction: Boolean, context: Context): Intent {
            return Intent(context, AppWatcherActivity::class.java).apply {
                putExtra(ARG_SORT, sortId)
                putExtra(ARG_SHOW_ACTION, showImportAction)
            }
        }

    }
}