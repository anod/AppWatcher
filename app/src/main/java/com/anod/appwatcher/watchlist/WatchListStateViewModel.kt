package com.anod.appwatcher.watchlist

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation3.runtime.NavKey
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.toAndroidAccount
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.SyncProgress
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.color.MaterialColors
import com.anod.appwatcher.utils.forMyApps
import com.anod.appwatcher.utils.getInt
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.utils.syncProgressFlow
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.PinShortcut
import info.anodsplace.framework.content.PinShortcutManager
import info.anodsplace.framework.content.ShowToastActionDefaults
import info.anodsplace.framework.content.StartActivityAction
import info.anodsplace.graphics.toIcon
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

@Immutable
data class WatchListSharedState(
    val tag: Tag,
    val sortId: Int,
    val filterId: Int,
    val titleFilter: String = "",
    val showSearch: Boolean = false,
    val initialRefreshing: Boolean = false,
    val syncProgress: SyncProgress? = null,
    val wideLayout: FoldableDeviceLayout = FoldableDeviceLayout(isWideLayout = false, hinge = Rect()),
    val showAppTagDialog: Boolean = false,
    val showEditTagDialog: Boolean = false,
    val tagAppsChange: Int = 0,
    val dbAppsChange: Int = 0,
    val recentlyInstalledApps: ImmutableList<App>? = null,
    val refreshRequest: Int = 0,
    val enablePullToRefresh: Boolean = false,
    val isRequestPinShortcutSupported: Boolean = false
)

sealed interface WatchListEvent {
    data object OnBackPressed : WatchListEvent
    data object PlayStoreMyApps : WatchListEvent
    data object Refresh : WatchListEvent

    data object SearchSubmit : WatchListEvent
    class ShowSearch(val show: Boolean) : WatchListEvent

    class ChangeSort(val sortId: Int) : WatchListEvent
    class FilterByTitle(val query: String) : WatchListEvent
    class SetWideLayout(val layout: FoldableDeviceLayout) : WatchListEvent
    class FilterById(val filterId: Int) : WatchListEvent
    class AddAppToTag(val show: Boolean) : WatchListEvent
    class EditTag(val show: Boolean) : WatchListEvent
    class SelectApp(val app: App) : WatchListEvent
    class UpdateSyncProgress(val syncProgress: SyncProgress) : WatchListEvent
    data object PinTagShortcut : WatchListEvent

    class AppClick(val app: App, val index: Int) : WatchListEvent
    class EmptyButton(val idx: Int) : WatchListEvent
    class AppLongClick(val app: App, val index: Int) : WatchListEvent
    class SectionHeaderClick(val type: SectionHeader) : WatchListEvent
}

sealed interface WatchListAction {
    data class StartActivity(override val intent: Intent) : WatchListAction, StartActivityAction
    class ShowToast(resId: Int, text: String, length: Int) : ShowToastActionDefaults(resId, text, length), WatchListAction
    data class SelectApp(val app: App) : WatchListAction
    data class NavigateTo(val navKey: NavKey): WatchListAction
    data object NavigateBack : WatchListAction
}

private fun startActivityAction(intent: Intent): WatchListAction
    = WatchListAction.StartActivity(intent)

private fun showToastAction(resId: Int = 0, text: String = "", length: Int = Toast.LENGTH_SHORT): WatchListAction
    = WatchListAction.ShowToast(
        resId = resId,
        text = text,
        length = length
    )

class WatchListStateViewModel(
    state: SavedStateHandle,
    tag: Tag,
    defaultFilterId: Int,
    collectRecentlyInstalledApps: Boolean,
    wideLayout: FoldableDeviceLayout
) : BaseFlowViewModel<WatchListSharedState, WatchListEvent, WatchListAction>(), KoinComponent {
    private val authToken: AuthTokenBlocking by inject()
    private val application: Application by inject()
    private val db: AppsDatabase by inject()
    private val packageChangedReceiver: PackageChangedReceiver by inject()
    private val recentlyInstalledAppsLoader: RecentlyInstalledAppsLoader by inject()
    private val packageManager: PackageManager by inject()
    private val shortcutManager: PinShortcutManager by inject()

    val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager))

    class Factory(
        private val defaultFilterId: Int,
        private val wideLayout: FoldableDeviceLayout,
        private val collectRecentlyInstalledApps: Boolean,
        private val initialTag: Tag
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val state = extras.createSavedStateHandle()
            return WatchListStateViewModel(
                state = state,
                tag = initialTag,
                defaultFilterId = defaultFilterId,
                wideLayout = wideLayout,
                collectRecentlyInstalledApps = collectRecentlyInstalledApps
            ) as T
        }
    }

    init {
        val expandSearch = state.remove("expand_search") ?: false
        val fromNotification = state.remove("extra_noti") ?: false
        val filterId = if (fromNotification || expandSearch) defaultFilterId else state.getInt("tab_id", defaultFilterId)
        viewState = WatchListSharedState(
            tag = tag,
            sortId = prefs.sortIndex,
            filterId = filterId,
            showSearch = expandSearch,
            wideLayout = wideLayout,
            enablePullToRefresh = prefs.enablePullToRefresh,
            isRequestPinShortcutSupported = if (!tag.isEmpty) shortcutManager.isSupported else false
        )

        AppLog.d("Initial state: viewState")

        viewModelScope.launch {
            syncProgressFlow(application).collect {
                handleEvent(WatchListEvent.UpdateSyncProgress(syncProgress = it))
            }
        }

        if (viewState.tag.isEmpty) {
            AppLog.d("mark updates as viewed.")
            prefs.isLastUpdatesViewed = true
        }

        if (!viewState.tag.isEmpty) {
            viewModelScope.launch {
                db.tags()
                    .observeTag(viewState.tag.id)
                    .collect { tag ->
                        if (tag == null) {
                            // TODO: emitAction(CommonActivityAction.Finish.toWatchListAction())
                        } else {
                            viewState = viewState.copy(
                                tag = tag,
                                isRequestPinShortcutSupported = if (!tag.isEmpty) shortcutManager.isSupported else false
                            )
                        }
                    }
            }

            viewModelScope.launch {
                db.appTags()
                    .forTag(viewState.tag.id)
                    .drop(1) // skip initial load
                    .collect {
                        viewState = viewState.copy(tagAppsChange = viewState.tagAppsChange + 1)
                    }
            }
        }

        viewModelScope.launch {
            AppListTable.Queries.changes(db.apps())
                .drop(1)
                .map { (System.currentTimeMillis() / 1000).toInt() }
                .filter { it > 0 }
                .onEach { delay(600) }
                .flowOn(Dispatchers.Default)
                .collect {
                    viewState = viewState.copy(dbAppsChange = viewState.dbAppsChange + 1)
                }
        }

        if (collectRecentlyInstalledApps) {
            viewModelScope.launch {
                viewStates.map { "${it.refreshRequest}-${it.dbAppsChange}" }
                    .combine(packageChangedReceiver.observer.onStart { emit("") }) { viewStateChange, packageName -> "$viewStateChange-$packageName" }
                    .distinctUntilChanged()
                    .map {
                        recentlyInstalledAppsLoader.load(limit = 20)
                    }
                    .collect {
                        viewState = viewState.copy(recentlyInstalledApps = it.toPersistentList())
                    }
            }
        }
    }

    override fun handleEvent(event: WatchListEvent) {
        when (event) {
            is WatchListEvent.ChangeSort -> {
                prefs.sortIndex = event.sortId
                viewState = viewState.copy(sortId = event.sortId)
            }

            is WatchListEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            is WatchListEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.layout)
            is WatchListEvent.AddAppToTag -> viewState = viewState.copy(showAppTagDialog = event.show)
            is WatchListEvent.FilterById -> viewState = viewState.copy(filterId = event.filterId)
            is WatchListEvent.EditTag -> viewState = viewState.copy(showEditTagDialog = event.show)
            is WatchListEvent.ShowSearch -> viewState = viewState.copy(showSearch = event.show)
            is WatchListEvent.SelectApp -> emitAction(WatchListAction.SelectApp(event.app))
            WatchListEvent.OnBackPressed -> emitAction(WatchListAction.NavigateBack)

            is WatchListEvent.SearchSubmit -> {
                val query = viewState.titleFilter
                viewState = viewState.copy(showSearch = false, titleFilter = "")
                emitAction(WatchListAction.NavigateTo(
                    SceneNavKey.Search(keyword = query, focus = true, initiateSearch = true)
                ))
            }

            is WatchListEvent.UpdateSyncProgress -> {
                viewState = viewState.copy(syncProgress = event.syncProgress)
                if (event.syncProgress.isRefreshing) {
                    emitAction(showToastAction(resId = R.string.refresh_scheduled))
                } else {
                    if (event.syncProgress.updatesCount == 0) {
                        emitAction(showToastAction(resId = R.string.no_updates_found))
                    }
                }
            }
            WatchListEvent.PlayStoreMyApps -> emitAction(startActivityAction(
                intent = Intent().forMyApps(true),
            ))
            WatchListEvent.Refresh -> refresh()
            is WatchListEvent.AppClick -> {
                emitAction(WatchListAction.SelectApp(event.app))
            }
            is WatchListEvent.AppLongClick -> {}
            is WatchListEvent.EmptyButton -> {
                when (event.idx) {
                    1 -> emitAction(WatchListAction.NavigateTo(
                        SceneNavKey.Search(focus = true,)
                    ))
                    2 -> emitAction(WatchListAction.NavigateTo(SceneNavKey.Installed(importMode = true)))
                    3 -> emitAction(startActivityAction(
                        intent = Intent.makeMainActivity(ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"))
                    ))
                }
            }
            is WatchListEvent.SectionHeaderClick -> {
                when (event.type) {
                    SectionHeader.RecentlyInstalled -> emitAction(WatchListAction.NavigateTo(
                        SceneNavKey.Installed(importMode = false)
                    ))
                    else -> { }
                }
            }

            WatchListEvent.PinTagShortcut -> pinTagShortcut()
        }
    }

    private fun pinTagShortcut() {
        val intent = AppWatcherActivity.tagShortcutIntent(viewState.tag.id, viewState.tag.color, application)
        viewModelScope.launch {
            try {
                val icon = createTagIcon()
                shortcutManager.create(PinShortcut(
                    id = "appwatcher-tag-${viewState.tag.id}",
                    title = viewState.tag.name,
                    intent = intent,
                    icon = icon
                ))
            } catch (e: Exception) {
                AppLog.e(e)
                emitAction(showToastAction(
                    resId = R.string.unable_pin_shortcut,
                    length = Toast.LENGTH_SHORT
                ))
            }
        }
    }

    private suspend fun createTagIcon(): Icon = withContext(Dispatchers.Default) {
        val roles = MaterialColors.getColorRoles(viewState.tag.color, false)
        val background = roles.accent.toDrawable()
        val foreground: Drawable = ContextCompat.getDrawable(application, R.drawable.shortcut_tag)!!
        foreground.setTint(roles.onAccent)
        return@withContext AdaptiveIconDrawable(background, foreground).toIcon(application)
    }

    private fun refresh() {
        val isRefreshing = viewState.syncProgress?.isRefreshing == true
        if (!isRefreshing) {
            appScope.launch {
                try {
                    requestRefresh()
                } catch (_: Exception) {
                    if (networkConnection.isNetworkAvailable) {
                        emitAction(showToastAction(
                            resId = R.string.failed_gain_access,
                            length = Toast.LENGTH_SHORT
                        ))
                    } else {
                        emitAction(showToastAction(
                            resId = R.string.check_connection,
                            length = Toast.LENGTH_SHORT
                        ))
                    }
                    viewState = viewState.copy(syncProgress = null)
                }
            }
        }
    }

    private suspend fun requestRefresh() {
        AppLog.d("Refresh requested")
        val account = prefs.account ?: throw IllegalStateException("account is null")
        if (authToken.checkToken(account.toAndroidAccount()) is Error) {
            throw IllegalStateException("auth token is invalid")
        }

        viewState = viewState.copy(
            syncProgress = SyncProgress(true, 0),
            refreshRequest = viewState.refreshRequest + 1
        )
        installedApps.reset()
        SyncScheduler(application).execute().first()
    }
}