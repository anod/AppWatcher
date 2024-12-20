package com.anod.appwatcher.details

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.text.format.Formatter
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.palette.graphics.Palette
import coil3.asDrawable
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.accounts.CheckTokenResult
import com.anod.appwatcher.accounts.toAndroidAccount
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.androidVersions
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.forPlayStore
import com.anod.appwatcher.utils.prefs
import finsky.api.DfeApi
import finsky.api.Document
import finsky.api.toDocument
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.content.forUninstall
import info.anodsplace.framework.text.Html
import info.anodsplace.graphics.chooseDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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

@Immutable
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
    val changelogState: ChangelogLoadState = ChangelogLoadState.Initial,
    val changelogs: List<AppChange> = emptyList(),
    val tagsMenuItems: List<TagMenuItem> = emptyList(),
    val customPrimaryColor: Int? = null,
    val document: Document? = null,
    val remoteVersionInfo: AppVersionInfo? = null,
    val remoteCallFinished: Boolean = false,
    val packageInfo: InstalledApps.Info = InstalledApps.Info(0, ""),
    val isSystemInDarkTheme: Boolean = false
) {
    val isWatched: Boolean
        get() = app != null && app.status != App.STATUS_DELETED && app.rowId > 0

    val fetchedRemoteDocument: Boolean
        get() = document != null
}

@Immutable
data class AppVersionInfo(
    val isBeta: Boolean,
    val installationSize: Long,
    val targetSdkVersion: Int,
    val starRating: Float,
) {
    val androidVersion: String? = androidVersions[targetSdkVersion]
}

sealed interface DetailsAction {
    class ActivityAction(val action: CommonActivityAction) : DetailsAction
    class ShowTagSnackbar(val appInfo: App) : DetailsAction
    object Dismiss : DetailsAction
    class Share(val app: App, val recentChange: AppChange) : DetailsAction
}

private fun startActivityAction(intent: Intent, addMultiWindowFlags: Boolean = false): DetailsAction.ActivityAction {
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
    object App : DetailsEvent
    object PlayStore : DetailsEvent
    object Translate : DetailsEvent
}

class DetailsViewModel(
    app: App,
    isSystemInDarkTheme: Boolean
): BaseFlowViewModel<DetailsState, DetailsEvent, DetailsAction>(), KoinComponent {

    class Factory(
        private val argApp: App,
        private val isSystemInDarkTheme: Boolean
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return DetailsViewModel(
                argApp,
                isSystemInDarkTheme
            ) as T
        }
    }

    private val context: Context by inject()
    private val database: AppsDatabase by inject()
    private val authToken: AuthTokenBlocking by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val iconLoader: AppIconLoader by inject()
    private val packageManager: PackageManager by inject()
    private val dfeApi: DfeApi by inject()

    val installedApps: InstalledApps = InstalledApps.PackageManager(packageManager)

    init {
        viewState = DetailsState(
            appId = app.appId,
            rowId = app.rowId,
            detailsUrl = if (app.detailsUrl.isNullOrEmpty()) App.createDetailsUrl(app.packageName) else app.detailsUrl,
            app = app,
            appIconState = if (app.iconUrl.isEmpty()) AppIconState.Default else AppIconState.Initial,
            title = app.title,
            isLocalApp = app.rowId == -1,
            packageInfo = installedApps.packageInfo(app.packageName),
            isSystemInDarkTheme = isSystemInDarkTheme
        )

        if (app.iconUrl.isNotEmpty()) {
            loadAppIcon(app.iconUrl)
        }
        observeApp()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeApp() {
        viewModelScope.launch {
            database.apps().observeApp(viewState.appId).collect { app ->
                viewState = if (app == null && viewState.rowId == -1) {
                    AppLog.i("Show details for unwatched ${viewState.appId}", "DetailsView")
                    viewState.copy(
                        appLoadingState = AppLoadingState.Loaded,
                    )
                } else {
                    AppLog.i("Show details for watched ${viewState.appId}", "DetailsView")
                    viewState.copy(
                        appLoadingState = if (app == null) AppLoadingState.NotFound else AppLoadingState.Loaded,
                        app = app,
                        rowId = app?.rowId ?: -1
                    )
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
            val drawable = iconLoader.get(iconUrl)?.asDrawable(context.resources) as? BitmapDrawable
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
                customPrimaryColor = customPrimaryColor ?: if (viewState.isSystemInDarkTheme) Color.Black.toArgb() else Color.White.toArgb()
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
                            .updateStatus(rowId = viewState.rowId, App.STATUS_DELETED)
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

            DetailsEvent.App ->
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
                emitAction(
                    DetailsAction.Share(
                        app = viewState.app!!,
                        recentChange = viewState.changelogs.firstOrNull() ?: AppChange.empty
                    )
                )
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
                        Uri.parse("https://translate.google.com/?sl=auto&tl=$lang&text=$encoded&op=translate")
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
            if (viewState.appId.isBlank()) {
                emptyList()
            } else {
                database.changelog()
                    .ofApp(viewState.appId)
            }
        } catch (e: Exception) {
            AppLog.e("loadChangelog", e)
            emptyList()
        }

        val account = prefs.account?.toAndroidAccount()
        viewState = viewState.copy(
            changelogState = if (localChanges.isEmpty() || account == null) ChangelogLoadState.Initial else ChangelogLoadState.Complete,
            changelogs = localChanges
        )

        if (account != null) {
            var loadError = false
            try {
                if (authToken.checkToken(account) is CheckTokenResult.Success) {
                    try {
                        val document = dfeApi.details(viewState.detailsUrl).toDocument()
                        onRemoteDetailsFetched(localChanges, document)
                    } catch (e: Exception) {
                        loadError = true
                        AppLog.e("Cannot fetch details for ${viewState.appId} , detailsUrl: ${viewState.detailsUrl}", e)
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
                val info = App(document, uploadDateParserCache)
                AppListTable.Queries.insertSafetly(info, database)
            }
            when (result) {
                AppListTable.ERROR_INSERT -> emitAction(action = showToastAction(resId = R.string.error_insert_app))
                AppListTable.ERROR_ALREADY_ADDED -> emitAction(action = showToastAction(resId = R.string.app_already_added))
                else -> emitAction(
                    DetailsAction.ShowTagSnackbar(
                        appInfo = App(
                            document!!,
                            uploadDateParserCache
                        )
                    )
                )
            }
        }
    }

    private fun onRemoteDetailsFetched(localChanges: List<AppChange>, document: Document?) {
        val appDetails = document?.appDetails

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
                document = document,
                changelogs = mergeChangelogs(localChanges, recentChange),
                changelogState = ChangelogLoadState.Complete,
                app = if (viewState.rowId == -1) App(document, uploadDateParserCache) else viewState.app,
                title = viewState.app?.title ?: viewState.title,
                remoteCallFinished = true,
                remoteVersionInfo = AppVersionInfo(
                    isBeta = when {
                        appDetails.testingProgramInfo.subscribed -> true
                        appDetails.testingProgramInfo.subscribedAndInstalled -> true
                        else -> false
                    },
                    installationSize = appDetails.fileList.sumOf { it.compressedSize },
                    targetSdkVersion = appDetails.targetSdkVersion,
                    starRating = document.rating.starRating
                )
            )
        } else {
            viewState.copy(
                changelogState = ChangelogLoadState.Complete,
                remoteCallFinished = true,
            )
        }

        appDetails?.fileList?.forEachIndexed { index, fileMetadata ->
            AppLog.d("FileList #$index " +
                "compressedSize: ${Formatter.formatShortFileSize(context, fileMetadata.compressedSize)} " +
                "size: ${Formatter.formatShortFileSize(context, fileMetadata.size)} " +
                "metadata: $fileMetadata")
        }

        viewState.remoteVersionInfo?.installationSize?.also {
            AppLog.d("FileList Sum compressedSize: ${Formatter.formatShortFileSize(context, it)}")
        }
    }

    private fun mergeChangelogs(localChanges: List<AppChange>, recentChange: AppChange): List<AppChange> {
        return when {
            localChanges.isEmpty() -> {
                if (!recentChange.isEmpty) {
                    listOf(recentChange)
                } else {
                    listOf()
                }
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