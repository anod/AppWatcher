package com.anod.appwatcher.details

import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.palette.graphics.Palette
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.forPlayStore
import com.anod.appwatcher.utils.prefs
import finsky.api.model.DfeDetails
import finsky.api.model.Document
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.graphics.chooseDark
import info.anodsplace.playstore.DetailsEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

typealias TagMenuItem = Pair<Tag, Boolean>

sealed class ChangelogLoadState {
    object Initial : ChangelogLoadState()
    object LocalComplete : ChangelogLoadState()
    class RemoteComplete(val error: Boolean) : ChangelogLoadState()
    object Complete : ChangelogLoadState()
}

sealed class AppLoadingState {
    object Initial : AppLoadingState()
    object Loaded : AppLoadingState()
    object NotFound : AppLoadingState()
}

sealed class AppIconState {
    object Initial : AppIconState()
    class Loaded(val drawable: BitmapDrawable) : AppIconState()
    object Default : AppIconState()
}

data class DetailsScreenState(
        val appId: String,
        val rowId: Int,
        val detailsUrl: String,
        val appLoadingState: AppLoadingState = AppLoadingState.Initial,
        val app: App? = null,
        val isLocalApp: Boolean = false,
        val title: String = "",
        val appIconState: AppIconState = AppIconState.Initial,
        val palette: Palette? = null,
        val account: Account? = null,
        val changelogState: ChangelogLoadState = ChangelogLoadState.Initial,
        val localChangelog: List<AppChange> = emptyList(),
        val recentChange: AppChange = AppChange("", 0, "", "", "", false),
        val errorShown: Boolean = false,
        val tagsMenuItems: List<TagMenuItem> = emptyList(),
        val customPrimaryColor: Int? = null,
        val document: Document? = null,
        val isInstalled: Boolean = false
) {
    val isWatched: Boolean
        get() = app != null && app.status != AppInfoMetadata.STATUS_DELETED
}

sealed interface DetailsScreenAction {
    class ActivityAction(val action: CommonActivityAction) : DetailsScreenAction
    object Dismiss : DetailsScreenAction
    object Share : DetailsScreenAction
}

private fun startActivityAction(intent: Intent, addMultiWindowFlags: Boolean = false) : DetailsScreenAction.ActivityAction {
    return DetailsScreenAction.ActivityAction(
        action = CommonActivityAction.StartActivity(
            intent = intent,
            addMultiWindowFlags = addMultiWindowFlags
        )
    )
}

private fun showToastAction(@StringRes resId: Int): DetailsScreenAction {
    return DetailsScreenAction.ActivityAction(
        action = CommonActivityAction.ShowToast(
            resId = resId
        )
    )
}

sealed interface DetailsScreenEvent {
    class UpdateTag(val tagId: Int, val checked: Boolean) : DetailsScreenEvent
    object WatchAppToggle : DetailsScreenEvent
    object LoadChangelog : DetailsScreenEvent
    object ReloadChangelog : DetailsScreenEvent
    object OnBackPressed : DetailsScreenEvent
    object Share : DetailsScreenEvent
    object Open : DetailsScreenEvent
    object Uninstall : DetailsScreenEvent
    object AppInfo : DetailsScreenEvent
    object PlayStore : DetailsScreenEvent
}

class DetailsViewModel(argAppId: String, argRowId: Int, argDetailsUrl: String) : BaseFlowViewModel<DetailsScreenState, DetailsScreenEvent, DetailsScreenAction>(), KoinComponent {

    class Factory(
            private val argAppId: String,
            private val argRowId: Int,
            private val argDetailsUrl: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return DetailsViewModel(argAppId, argRowId, argDetailsUrl) as T
        }
    }

    private val context: ApplicationContext by inject()
    private val database: AppsDatabase by inject()
    private val authToken: AuthTokenBlocking by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val iconLoader: AppIconLoader by inject()
    private val packageManager: PackageManager by inject()

    private val detailsEndpoint: DetailsEndpoint by inject { parametersOf(viewState.detailsUrl) }

    val installedApps: InstalledApps by lazy { InstalledApps.PackageManager(packageManager) }

    init {
        val isInstalled = installedApps.packageInfo(argAppId).isInstalled
        viewState = DetailsScreenState(appId = argAppId, rowId = argRowId, detailsUrl = argDetailsUrl, account = prefs.account, isInstalled = isInstalled)

        observeApp()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeApp() {
        viewModelScope.launch {
            database.apps().observeApp(viewState.appId).collect { app ->
                if (app == null && viewState.rowId == -1) {
                    AppLog.i("Show details for unwatched ${viewState.appId}", "DetailsView")
                    val localApp = packageManager.packageToApp(-1, viewState.appId)
                    viewState = viewState.copy(
                            appLoadingState = AppLoadingState.Loaded,
                            app = localApp,
                            appIconState = if (localApp.iconUrl.isEmpty()) AppIconState.Default else viewState.appIconState,
                            title = localApp.generateTitle(context.resources).toString(),
                            isLocalApp = true
                    )
                    if (localApp.iconUrl.isNotEmpty()) {
                        loadAppIcon(localApp.iconUrl)
                    }
                } else {
                    AppLog.i("Show details for watched ${viewState.appId}", "DetailsView")
                    viewState = viewState.copy(
                        appLoadingState = if (app == null) AppLoadingState.NotFound else AppLoadingState.Loaded,
                        app = app,
                        appIconState = if (app?.iconUrl?.isNotEmpty() == true) viewState.appIconState else AppIconState.Default,
                        title = app?.generateTitle(context.resources)?.toString() ?: ""
                    )
                    if (app?.iconUrl?.isNotEmpty() == true) {
                        loadAppIcon(app.iconUrl)
                    }
                }
            }
        }

        viewModelScope.launch {
            database.tags().observe().flatMapLatest { tags ->
                return@flatMapLatest database.appTags().forApp(viewState.appId).map { appTags ->
                    val appTagsList = appTags.map { it.tagId }
                    tags.map { TagMenuItem(it, appTagsList.contains(it.id)) }
                }
            }.collect { tagsMenuItems ->
                viewState = viewState.copy(tagsMenuItems = tagsMenuItems)
            }
        }
    }

    private fun loadAppIcon(iconUrl: String) {
        viewModelScope.launch {
            val drawable = iconLoader.get(iconUrl) as? BitmapDrawable
            if (drawable == null) {
                viewState = viewState.copy(appIconState = AppIconState.Default)
                return@launch
            }
            val customPrimaryColor = withContext(Dispatchers.Default) {
                val palette = Palette.from(drawable.bitmap).generate()
                val darkSwatch = palette.chooseDark()
                darkSwatch?.rgb
            }
            viewState = viewState.copy(
                appIconState = AppIconState.Loaded(drawable = drawable),
                customPrimaryColor = customPrimaryColor
            )
        }
    }

    override fun handleEvent(event: DetailsScreenEvent) {
        when (event) {
            is DetailsScreenEvent.UpdateTag -> changeTag(event.tagId, event.checked)
            DetailsScreenEvent.WatchAppToggle -> {
                if (viewState.isWatched) {
                    viewModelScope.launch {
                        database.apps().updateStatus(rowId = viewState.rowId, AppInfoMetadata.STATUS_DELETED)
                    }
                } else {
                    watchApp()
                }
            }
            DetailsScreenEvent.LoadChangelog -> loadChangelog()
            DetailsScreenEvent.ReloadChangelog -> {
                viewState = viewState.copy(changelogState = ChangelogLoadState.Initial)
                loadChangelog()
            }

            DetailsScreenEvent.AppInfo ->
                emitAction(startActivityAction(
                    intent = Intent().forAppInfo(viewState.appId),
                    addMultiWindowFlags = true
                ))
            DetailsScreenEvent.OnBackPressed -> emitAction(DetailsScreenAction.Dismiss)
            DetailsScreenEvent.Open -> {
                val launchIntent = packageManager.getLaunchIntentForPackage(viewState.appId)
                if (launchIntent != null) {
                    emitAction(startActivityAction(intent = launchIntent, addMultiWindowFlags = true))
                }
            }
            DetailsScreenEvent.Share -> emitAction(DetailsScreenAction.Share)
            DetailsScreenEvent.Uninstall -> emitAction(startActivityAction(
                intent = Intent().forUninstall(viewState.appId)
            ))

            DetailsScreenEvent.PlayStore ->  emitAction(startActivityAction(
                intent = Intent().forPlayStore(viewState.appId),
                addMultiWindowFlags = true
            ))
        }
    }

    private fun loadChangelog() {
        try {
            loadLocalChangelog()
        } catch (e: Exception) {
            AppLog.e("onResume", e)
        }

        prefs.account?.let { account ->
            viewModelScope.launch {
                try {
                    if (authToken.refreshToken(account)) {
                        loadRemoteChangelog()
                    } else {
                        AppLog.e("Error retrieving token")
                        loadRemoteChangelog()
                    }
                } catch (e: AuthTokenStartIntent) {
                    emitAction(startActivityAction(e.intent, addMultiWindowFlags = true))
                } catch (e: Exception) {
                    AppLog.e("onResume", e)
                }
            }
        }
    }

    private fun loadLocalChangelog() {
        if (viewState.appId.isBlank()) {
            viewState = viewState.copy(changelogState = mergeChangelogState(ChangelogLoadState.LocalComplete))
            return
        }
        viewModelScope.launch {
            val localChangelog = database.changelog().ofApp(viewState.appId)
            viewState = viewState.copy(localChangelog = localChangelog, changelogState = mergeChangelogState(ChangelogLoadState.LocalComplete))
        }
    }

    private fun loadRemoteChangelog() {
        if (authToken.token.isBlank()) {
            viewState = viewState.copy(changelogState = mergeChangelogState(ChangelogLoadState.RemoteComplete(true)))
        } else {
            viewModelScope.launch {
                try {
                    val model = detailsEndpoint.start()
                    onDataChanged(model)
                } catch (e: Exception) {
                    AppLog.e("Cannot fetch details for ${viewState.appId}", e)
                    viewState = viewState.copy(changelogState = mergeChangelogState(ChangelogLoadState.RemoteComplete(true)))
                }
            }
        }
    }

    private fun changeTag(tagId: Int, checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                database.appTags().delete(tagId, viewState.appId) > 0
            } else {
                AppTagsTable.Queries.insert(tagId, viewState.appId, database)
            }
        }
    }

    private fun watchApp() {
        viewModelScope.launch {
            val document = viewState.document
            val result = if (document == null) {
                AppListTable.ERROR_INSERT
            } else {
                val info = AppInfo(document, uploadDateParserCache)
                AppListTable.Queries.insertSafetly(info, database)
            }
            when (result) {
                AppListTable.ERROR_INSERT -> emitAction(action = showToastAction(resId = R.string.error_insert_app))
                AppListTable.ERROR_ALREADY_ADDED -> emitAction(action = showToastAction(resId = R.string.app_already_added))
                else -> {
                    val info = AppInfo(document!!, uploadDateParserCache)
                    // TODO: TagSnackbar.make(view, info, false, requireActivity(), viewModel.prefs).show()
                }
            }
        }
    }

    private fun onDataChanged(details: DfeDetails) {
        val appDetails = details.document?.appDetails

        if (appDetails != null) {
            val app = viewState.app?.apply {
                testing = when {
                    appDetails.testingProgramInfo.subscribed -> 1
                    appDetails.testingProgramInfo.subscribedAndInstalled -> 2
                    else -> 0
                }
            }
            viewState = viewState.copy(
                    app = app,
                    recentChange = AppChange(viewState.appId, appDetails.versionCode, appDetails.versionString, appDetails.recentChangesHtml
                            ?: "", appDetails.uploadDate, false),
                    changelogState = mergeChangelogState(ChangelogLoadState.RemoteComplete(false)),
                    document = details.document
            )
        } else {
            viewState = viewState.copy(changelogState = mergeChangelogState(ChangelogLoadState.RemoteComplete(false)))
        }
    }

    private fun mergeChangelogState(newChangelogState: ChangelogLoadState): ChangelogLoadState {
        val changelogState = viewState.changelogState
        return when (newChangelogState) {
            is ChangelogLoadState.LocalComplete -> if (changelogState is ChangelogLoadState.RemoteComplete || changelogState is ChangelogLoadState.Complete) {
                ChangelogLoadState.Complete
            } else {
                newChangelogState
            }
            is ChangelogLoadState.RemoteComplete -> if (changelogState is ChangelogLoadState.LocalComplete || changelogState is ChangelogLoadState.Complete) {
                return ChangelogLoadState.Complete
            } else {
                return newChangelogState
            }
            ChangelogLoadState.Complete -> ChangelogLoadState.Complete
            ChangelogLoadState.Initial -> ChangelogLoadState.Initial
        }
    }

}