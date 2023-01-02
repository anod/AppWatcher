package com.anod.appwatcher.details

import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.palette.graphics.Palette
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.prefs
import com.google.android.material.color.ColorRoles
import com.google.android.material.color.MaterialColors
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
        val title: String = "",
        val appIconState: AppIconState = AppIconState.Initial,
        val palette: Palette? = null,
        val account: Account? = null,
        val changelogState: ChangelogLoadState = ChangelogLoadState.Initial,
        val localChangelog: List<AppChange> = emptyList(),
        val recentChange: AppChange = AppChange("", 0, "", "", "", false),
        val errorShown: Boolean = false,
        val tagsMenuItems: List<TagMenuItem> = emptyList(),
        val accentColorRoles: ColorRoles? = null,
        val document: Document? = null,
        val isInstalled: Boolean = false
)

sealed interface DetailsScreenAction {
    object Dismiss : DetailsScreenAction
    object Share : DetailsScreenAction
    class WatchAppResult(val result: Int) : DetailsScreenAction
    class StartActivity(val intent: Intent, val addMultiWindowFlags: Boolean = false) : DetailsScreenAction
}

sealed interface DetailsScreenEvent {
    class UpdateTag(val tagId: Int, val checked: Boolean) : DetailsScreenEvent
    object WatchApp : DetailsScreenEvent
    object LoadChangelog : DetailsScreenEvent
    object ReloadChangelog : DetailsScreenEvent
    object OnBackPressed : DetailsScreenEvent
    object Share : DetailsScreenEvent
    object Open : DetailsScreenEvent
    object Uninstall : DetailsScreenEvent
    object AppInfo : DetailsScreenEvent
}

class DetailsViewModel(argAppId: String, argRowId: Int, argDetailsUrl: String) : BaseFlowViewModel<DetailsScreenState, DetailsScreenEvent, DetailsScreenAction>(), KoinComponent {

    class Factory(
            private val argAppId: String,
            private val argRowId: Int,
            private val argDetailsUrl: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return DetailsViewModel(argAppId, argRowId, argDetailsUrl) as T
        }
    }

    val context: ApplicationContext by inject()
    val database: AppsDatabase by inject()
    val authToken: AuthTokenBlocking by inject()
    val uploadDateParserCache: UploadDateParserCache by inject()
    private val iconLoader: AppIconLoader by inject()
    val packageManager: PackageManager by inject()
    val installedApps: InstalledApps by lazy { InstalledApps.PackageManager(packageManager) }

    private val detailsEndpoint: DetailsEndpoint by inject { parametersOf(viewState.detailsUrl) }

    init {
        val isInstalled = installedApps.packageInfo(argAppId).isInstalled
        viewState = DetailsScreenState(appId = argAppId, rowId = argRowId, detailsUrl = argDetailsUrl, account = prefs.account, isInstalled = isInstalled)
    }

    val appId: String
        get() = viewState.appId
    val rowId: Int
        get() = viewState.rowId

    var errorShown: Boolean
        get() = viewState.errorShown
        set(value) {
            viewState = viewState.copy(errorShown = value)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeApp() {
        viewModelScope.launch {
            database.apps().observeApp(appId).collect { app ->
                if (app == null && rowId == -1) {
                    AppLog.i("Show details for unwatched $appId", "DetailsView")
                    val localApp = packageManager.packageToApp(-1, appId)
                    viewState = viewState.copy(
                            appLoadingState = AppLoadingState.Loaded,
                            app = localApp,
                            appIconState = if (localApp.iconUrl.isEmpty()) AppIconState.Default else viewState.appIconState,
                            title = localApp.generateTitle(context.resources).toString()
                    )
                    if (localApp.iconUrl.isNotEmpty()) {
                        loadAppIcon(localApp.iconUrl)
                    }
                } else {
                    AppLog.i("Show details for watched $appId", "DetailsView")
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
                return@flatMapLatest database.appTags().forApp(appId).map { appTags ->
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
            val accentColorRoles = withContext(Dispatchers.Default) {
                val palette = Palette.from(drawable.bitmap).generate()
                val darkSwatch = palette.chooseDark()
                if (darkSwatch != null) MaterialColors.getColorRoles(darkSwatch.rgb, false) else null
            }
            viewState = viewState.copy(
                appIconState = AppIconState.Loaded(drawable = drawable),
                accentColorRoles = accentColorRoles
            )
        }
    }

    override fun handleEvent(event: DetailsScreenEvent) {
        when (event) {
            is DetailsScreenEvent.UpdateTag -> changeTag(event.tagId, event.checked)
            DetailsScreenEvent.WatchApp -> watchApp()
            DetailsScreenEvent.LoadChangelog -> loadChangelog()
            DetailsScreenEvent.ReloadChangelog -> {
                viewState = viewState.copy(changelogState = ChangelogLoadState.Initial)
                loadChangelog()
            }

            DetailsScreenEvent.AppInfo ->
                emitAction(DetailsScreenAction.StartActivity(
                    intent = Intent().forAppInfo(viewState.appId, context.actual)
                ))
            DetailsScreenEvent.OnBackPressed -> emitAction(DetailsScreenAction.Dismiss)
            DetailsScreenEvent.Open -> {
                val launchIntent = packageManager.getLaunchIntentForPackage(viewState.appId)
                if (launchIntent != null) {
                    emitAction(DetailsScreenAction.StartActivity(intent = launchIntent, addMultiWindowFlags = true))
                }
            }
            DetailsScreenEvent.Share -> emitAction(DetailsScreenAction.Share)
            DetailsScreenEvent.Uninstall -> emitAction(DetailsScreenAction.StartActivity(
                intent = Intent().forUninstall(viewState.appId)
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
                    emitAction(DetailsScreenAction.StartActivity(e.intent, true))
                } catch (e: Exception) {
                    AppLog.e("onResume", e)
                }
            }
        }
    }

    private fun loadLocalChangelog() {
        if (appId.isBlank()) {
            viewState = viewState.copy(changelogState = mergeChangelogState(ChangelogLoadState.LocalComplete))
            return
        }
        viewModelScope.launch {
            val localChangelog = database.changelog().ofApp(appId)
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
                    AppLog.e("Cannot fetch details for $appId", e)
                    viewState = viewState.copy(changelogState = mergeChangelogState(ChangelogLoadState.RemoteComplete(true)))
                }
            }
        }
    }

    private fun changeTag(tagId: Int, checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                database.appTags().delete(tagId, appId) > 0
            } else {
                AppTagsTable.Queries.insert(tagId, appId, database)
            }
        }
    }

    private fun watchApp() {
        viewModelScope.launch {
            val document = viewState.document
            if (document == null) {
                emitAction(DetailsScreenAction.WatchAppResult(AppListTable.ERROR_INSERT))
            } else {
                val info = AppInfo(document, uploadDateParserCache)
                val result = AppListTable.Queries.insertSafetly(info, database)
                emitAction(DetailsScreenAction.WatchAppResult(result))
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
                    recentChange = AppChange(appId, appDetails.versionCode, appDetails.versionString, appDetails.recentChangesHtml
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