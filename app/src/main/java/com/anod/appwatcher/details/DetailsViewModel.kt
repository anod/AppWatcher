package com.anod.appwatcher.details

import android.accounts.Account
import androidx.lifecycle.*
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.model.AppInfo
import finsky.api.model.DfeDetails
import finsky.api.model.Document
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.playstore.DetailsEndpoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

typealias TagMenuItem = Pair<Tag, Boolean>

sealed class ChangelogLoadState
object Initial : ChangelogLoadState()
object LocalComplete : ChangelogLoadState()
class RemoteComplete(val error: Boolean) : ChangelogLoadState()
object Complete : ChangelogLoadState()

sealed class AppLoadingState
class Loaded(val app: App) : AppLoadingState()
object NotFound : AppLoadingState()

class DetailsViewModel(application: android.app.Application) : AndroidViewModel(application) {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    val provide: AppComponent
        get() = Application.provide(context)

    val database: AppsDatabase
        get() = provide.database

    var detailsUrl = ""

    private val _appId = MutableStateFlow("")
    var appId: String
        get() = _appId.value
        set(value) { _appId.value = value }

    var rowId: Int = -1

    val appLoading: Flow<AppLoadingState> = _appId.filter { it.isNotEmpty() }.flatMapLatest {
        return@flatMapLatest database.apps().observeApp(it).map { app ->
            if (app == null && rowId == -1) {
                AppLog.i("Show details for unwatched $appId", "DetailsView")
                Loaded(context.packageManager.packageToApp(-1, appId))
            } else {
                AppLog.i("Show details for watched $appId", "DetailsView")
                if (app == null) NotFound else Loaded(app)
            }
        }
    }

    val app: StateFlow<App?> = appLoading.map {
        when (it) {
            is Loaded -> it.app
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val watchStateChange: MutableSharedFlow<Int> = MutableSharedFlow()

    val account: Account? by lazy {
        Application.provide(application).prefs.account
    }

    private val detailsEndpoint: DetailsEndpoint by lazy {
        val account = this.account ?: Account("empty", "empty")
        DetailsEndpoint(application, provide.networkClient, provide.deviceInfo, account, detailsUrl)
    }
    var authToken = ""
    var localChangelog: List<AppChange> = emptyList()
    val tagsMenuItems: StateFlow<List<TagMenuItem>> = flow {
        if (appId.isNotEmpty()) {
            emit(appId)
        }
    }.flatMapLatest tagsMenu@{ appId ->
        if (appId.isEmpty()) {
            return@tagsMenu flowOf(emptyList<TagMenuItem>())
        }
        return@tagsMenu database.tags().observe().flatMapLatest { tags ->
            return@flatMapLatest database.appTags().forApp(appId).map { appTags ->
                val appTagsList = appTags.map { it.tagId }
                tags.map { TagMenuItem(it, appTagsList.contains(it.id)) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val changelogState = MutableStateFlow<ChangelogLoadState>(Initial)
    val document: Document?
        get() = detailsEndpoint.document
    var recentChange = AppChange(appId, 0, "", "", "", false)

    fun loadLocalChangelog() {
        if (appId.isBlank()) {
            this.updateChangelogState(LocalComplete)
            return
        }
        viewModelScope.launch {
            localChangelog = database.changelog().ofApp(appId)
            updateChangelogState(LocalComplete)
        }
    }

    fun loadRemoteChangelog() {
        if (this.authToken.isBlank()) {
            this.updateChangelogState(RemoteComplete(true))
        } else {
            detailsEndpoint.authToken = this.authToken
            viewModelScope.launch {
                try {
                    val model = detailsEndpoint.start()
                    onDataChanged(model)
                } catch (e: Exception) {
                    onErrorResponse(e)
                }
            }
        }
    }

    private fun onDataChanged(details: DfeDetails) {
        val appDetails = details.document?.appDetails
        if (appDetails != null) {
            recentChange = AppChange(appId, appDetails.versionCode, appDetails.versionString, appDetails.recentChangesHtml
                    ?: "", appDetails.uploadDate, false)
            app.value!!.testing = when {
                appDetails.testingProgramInfo.subscribed -> 1
                appDetails.testingProgramInfo.subscribedAndInstalled -> 2
                else -> 0
            }
        }
        this.updateChangelogState(RemoteComplete(false))
    }

    private fun updateChangelogState(state: ChangelogLoadState) {
        when (state) {
            is LocalComplete -> {
                if (this.changelogState.value is RemoteComplete || this.changelogState.value is Complete) {
                    this.changelogState.value = Complete
                } else {
                    this.changelogState.value = state
                }
            }
            is RemoteComplete -> {
                if (this.changelogState.value is LocalComplete || this.changelogState.value is Complete) {
                    this.changelogState.value = Complete
                } else {
                    this.changelogState.value = state
                }
            }
            Complete -> {
            }
            Initial -> {
            }
        }
    }

    private fun onErrorResponse(error: Exception) {
        AppLog.e("Cannot fetch details for $appId - $error")
        this.updateChangelogState(RemoteComplete(true))
    }

    fun changeTag(tagId: Int, checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                provide.database.appTags().delete(tagId, appId) > 0
            } else {
                AppTagsTable.Queries.insert(tagId, appId, provide.database)
            }
        }
    }

    fun watch() {
        viewModelScope.launch {
            val document = this@DetailsViewModel.document
            if (document == null) {
                watchStateChange.emit(AppListTable.ERROR_INSERT)
            } else {
                val info = AppInfo(document)
                val result = AppListTable.Queries.insertSafetly(info, database)
                watchStateChange.emit(result)
            }
        }
    }
}