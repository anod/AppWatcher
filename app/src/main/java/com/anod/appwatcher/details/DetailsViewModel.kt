package com.anod.appwatcher.details

import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.framework.text.Html
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
import java.net.URLEncoder
import java.util.Locale

typealias TagMenuItem = Pair<Tag, Boolean>

sealed interface ChangelogLoadState {
    object Initial : ChangelogLoadState
    object RemoteError : ChangelogLoadState
    object Complete : ChangelogLoadState
}

sealed interface AppLoadingState {
    object Initial : AppLoadingState
    object Loaded : AppLoadingState
    object NotFound : AppLoadingState
}

sealed interface AppIconState {
    object Initial : AppIconState
    class Loaded(val drawable: BitmapDrawable) : AppIconState
    object Default : AppIconState
}

data class DetailsState(
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
    val changelogs: List<AppChange> = emptyList(),
    val tagsMenuItems: List<TagMenuItem> = emptyList(),
    val customPrimaryColor: Int? = null,
    val document: Document? = null,
    val remoteVersionInfo: AppVersionInfo? = null,
    val remoteCallFinished: Boolean = false,
    val isInstalled: Boolean = false
) {
    val isWatched: Boolean
        get() = app != null && app.status != AppInfoMetadata.STATUS_DELETED

    val fetchedRemoteDocument: Boolean
        get() = document != null
}

data class AppVersionInfo(
    val isBeta: Boolean,
    val installationSize: Long,
    val targetSdkVersion: Int,
    val starRating: Float,
)

sealed interface DetailsAction {
    class ActivityAction(val action: CommonActivityAction) : DetailsAction
    class ShowTagSnackbar(val appInfo: AppInfo) : DetailsAction
    object Dismiss : DetailsAction
    class Share(val app: App) : DetailsAction
}

private fun startActivityAction(
    intent: Intent,
    addMultiWindowFlags: Boolean = false
): DetailsAction.ActivityAction {
    return DetailsAction.ActivityAction(
        action = CommonActivityAction.StartActivity(
            intent = intent,
            addMultiWindowFlags = addMultiWindowFlags
        )
    )
}

private fun showToastAction(@StringRes resId: Int): DetailsAction {
    return DetailsAction.ActivityAction(
        action = CommonActivityAction.ShowToast(
            resId = resId
        )
    )
}

sealed interface DetailsEvent {
    class UpdateTag(val tagId: Int, val checked: Boolean) : DetailsEvent
    class OpenUrl(val url: String) : DetailsEvent
    object WatchAppToggle : DetailsEvent
    object LoadChangelog : DetailsEvent
    object ReloadChangelog : DetailsEvent
    object OnBackPressed : DetailsEvent
    object Share : DetailsEvent
    object Open : DetailsEvent
    object Uninstall : DetailsEvent
    object AppInfo : DetailsEvent
    object PlayStore : DetailsEvent
    object Translate : DetailsEvent
}

class DetailsViewModel(argAppId: String, argRowId: Int, argDetailsUrl: String) :
    BaseFlowViewModel<DetailsState, DetailsEvent, DetailsAction>(), KoinComponent {

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

    private val database: AppsDatabase by inject()
    private val authToken: AuthTokenBlocking by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val iconLoader: AppIconLoader by inject()
    private val packageManager: PackageManager by inject()
    private val detailsEndpoint: DetailsEndpoint by inject { parametersOf(viewState.detailsUrl) }

    val installedApps: InstalledApps by lazy { InstalledApps.PackageManager(packageManager) }

    init {
        val isInstalled = installedApps.packageInfo(argAppId).isInstalled
        viewState = DetailsState(
            appId = argAppId,
            rowId = argRowId,
            detailsUrl = argDetailsUrl,
            account = prefs.account,
            isInstalled = isInstalled
        )

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
                        title = localApp.title,
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
                        title = app?.title ?: "",
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

    override fun handleEvent(event: DetailsEvent) {
        when (event) {
            is DetailsEvent.UpdateTag -> changeTag(event.tagId, event.checked)
            DetailsEvent.WatchAppToggle -> {
                if (viewState.isWatched) {
                    viewModelScope.launch {
                        database.apps()
                            .updateStatus(rowId = viewState.rowId, AppInfoMetadata.STATUS_DELETED)
                    }
                } else {
                    watchApp()
                }
            }

            DetailsEvent.LoadChangelog -> {
                viewModelScope.launch {
                    loadChangelog()
                }
            }

            DetailsEvent.ReloadChangelog -> {
                viewState = viewState.copy(changelogState = ChangelogLoadState.Initial)
                viewModelScope.launch {
                    loadChangelog()
                }
            }

            DetailsEvent.AppInfo ->
                emitAction(
                    startActivityAction(
                        intent = Intent().forAppInfo(viewState.appId),
                        addMultiWindowFlags = true
                    )
                )

            DetailsEvent.OnBackPressed -> emitAction(DetailsAction.Dismiss)
            DetailsEvent.Open -> {
                val launchIntent = packageManager.getLaunchIntentForPackage(viewState.appId)
                if (launchIntent != null) {
                    emitAction(
                        startActivityAction(
                            intent = launchIntent,
                            addMultiWindowFlags = true
                        )
                    )
                }
            }

            DetailsEvent.Share -> if (viewState.app != null) {
                emitAction(DetailsAction.Share(app = viewState.app!!))
            }

            DetailsEvent.Uninstall -> emitAction(
                startActivityAction(
                    intent = Intent().forUninstall(viewState.appId)
                )
            )

            DetailsEvent.PlayStore -> emitAction(
                startActivityAction(
                    intent = Intent().forPlayStore(viewState.appId),
                    addMultiWindowFlags = true
                )
            )

            DetailsEvent.Translate -> {
                val lang = Locale.getDefault().language
                val text = viewState.changelogs.firstOrNull()?.details ?: ""
                val encoded = URLEncoder.encode(Html.parse(text).toString(), "utf-8")
                emitAction(startActivityAction((Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://translate.google.com/?sl=auto&tl=${lang}&text=${encoded}&op=translate")
                })))
            }

            is DetailsEvent.OpenUrl -> {
                emitAction(
                    action = startActivityAction(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(event.url)
                        )
                    )
                )
            }
        }
    }

    private suspend fun loadChangelog() {
        val localChanges = try {
            if (viewState.appId.isBlank()) emptyList() else database.changelog()
                .ofApp(viewState.appId)
        } catch (e: Exception) {
            AppLog.e("loadChangelog", e)
            emptyList()
        }

        val account = prefs.account
        viewState = viewState.copy(
            changelogState = if (localChanges.isEmpty() || account == null) ChangelogLoadState.Initial else ChangelogLoadState.Complete,
            changelogs = localChanges
        )

        if (account != null) {
            var loadError = false
            try {
                if (authToken.refreshToken(account)) {
                    try {
                        val model = detailsEndpoint.start()
                        onRemoteDetailsFetched(localChanges, model)
                    } catch (e: Exception) {
                        loadError = true
                        AppLog.e("Cannot fetch details for ${viewState.appId}", e)
                    }
                } else {
                    loadError = true
                    AppLog.e("Error retrieving token")
                }
            } catch (e: AuthTokenStartIntent) {
                loadError = true
                emitAction(startActivityAction(e.intent, addMultiWindowFlags = true))
            } catch (e: Exception) {
                loadError = true
                AppLog.e("loadChangelog", e)
            }

            if (loadError) {
                viewState = viewState.copy(
                    changelogState = if (viewState.changelogState is ChangelogLoadState.Initial) ChangelogLoadState.RemoteError else viewState.changelogState,
                    remoteCallFinished = true
                )
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
                else -> emitAction(
                    DetailsAction.ShowTagSnackbar(
                        appInfo = AppInfo(
                            document!!,
                            uploadDateParserCache
                        )
                    )
                )
            }
        }
    }

    private fun onRemoteDetailsFetched(localChanges: List<AppChange>, details: DfeDetails) {
        val appDetails = details.document?.appDetails

        viewState = if (appDetails != null) {
            val recentChange = AppChange(
                viewState.appId,
                appDetails.versionCode,
                appDetails.versionString,
                appDetails.recentChangesHtml ?: "",
                appDetails.uploadDate,
                false
            )

            viewState.copy(
                document = details.document,
                changelogs = mergeChangelogs(localChanges, recentChange),
                changelogState = ChangelogLoadState.Complete,
                title = viewState.app?.title ?: "",
                remoteCallFinished = true,
                remoteVersionInfo = AppVersionInfo(
                    isBeta = when {
                        appDetails.testingProgramInfo.subscribed -> true
                        appDetails.testingProgramInfo.subscribedAndInstalled -> true
                        else -> false
                    },
                    installationSize = appDetails.fileList.firstOrNull()?.compressedSize ?: 0L,
                    targetSdkVersion = appDetails.targetSdkVersion,
                    starRating = details.document!!.rating.starRating
                )
            )
        } else {
            viewState.copy(
                changelogState = ChangelogLoadState.Complete,
                remoteCallFinished = true,
            )
        }
    }

    private fun mergeChangelogs(
        localChanges: List<AppChange>,
        recentChange: AppChange
    ): List<AppChange> {
        return when {
            localChanges.isEmpty() -> {
                if (!recentChange.isEmpty) {
                    listOf(recentChange)
                } else listOf()
            }

            localChanges.first().versionCode == recentChange.versionCode -> {
                listOf(recentChange, *localChanges.subList(1, localChanges.size).toTypedArray())
            }

            else -> {
                if (recentChange.isEmpty) {
                    localChanges
                } else {
                    listOf(recentChange, *localChanges.toTypedArray())
                }
            }
        }
    }
}