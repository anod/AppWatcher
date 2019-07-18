package com.anod.appwatcher.details

import android.accounts.Account
import androidx.lifecycle.*
import com.android.volley.VolleyError
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
import finsky.api.model.DfeModel
import finsky.api.model.Document
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.livedata.OneTimeObserver
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint
import kotlinx.coroutines.launch

typealias TagMenuItem = Pair<Tag,Boolean>

sealed class ChangelogLoadState
object LocalComplete : ChangelogLoadState()
class RemoteComplete(val error: Boolean): ChangelogLoadState()
object Complete : ChangelogLoadState()

class DetailsViewModel(application: android.app.Application) : AndroidViewModel(application), PlayStoreEndpoint.Listener {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    val provide: AppComponent
        get() = Application.provide(context)

    val database: AppsDatabase
        get() = provide.database

    var detailsUrl = ""
    var appId = MutableLiveData<String>("")
    var rowId: Int = -1
    var isNewApp: Boolean = false

    val app: MutableLiveData<App> = MutableLiveData()

    val account: Account? by lazy {
        Application.provide(application).prefs.account
    }

    private val detailsEndpoint: DetailsEndpoint by lazy {
        val account = this.account ?: Account("empty", "empty")
        DetailsEndpoint(application, provide.requestQueue, provide.deviceInfo, account, detailsUrl)
    }
    var authToken = ""
    var localChangelog: List<AppChange> = emptyList()
    val tagsMenuItems: LiveData<List<TagMenuItem>> = appId.switchMap tagsMenu@ { appId ->
        if (appId.isEmpty()) {
            return@tagsMenu MutableLiveData(emptyList<TagMenuItem>())
        }
        return@tagsMenu database.tags().observe().switchMap { tags ->
            return@switchMap database.appTags().forApp(appId).map { appTags ->
                val appTagsList = appTags.map { it.tagId }
                tags.map { TagMenuItem(it, appTagsList.contains(it.id)) }
            }
        }
    }

    val changelogState = MutableLiveData<ChangelogLoadState>()

    val document: Document?
        get() = detailsEndpoint.document

    var recentChange = AppChange(appId.value!!, 0, "", "", "")

    override fun onCleared() {
        detailsEndpoint.listener = null
    }

    fun loadApp() {
        if (appId.value!!.isEmpty()) {
            app.value = null
            return
        }

        if (rowId == -1) {
            val localApp = context.packageManager.packageToApp(-1, appId.value!!)
            isNewApp = true
            AppLog.i("Show details for unwatched ${appId.value}")
            app.value = localApp
        } else {
            isNewApp = false
            AppLog.i("Show details for watched ${appId.value}")

            val appsTable = database.apps()
            viewModelScope.launch {
                app.value = appsTable.loadAppRow(rowId)
            }
        }
    }

    fun loadLocalChangelog() {
        if (appId.value!!.isBlank()) {
            this.updateChangelogState(LocalComplete)
            return
        }
        val changes = database.changelog().ofApp(appId.value!!)
        changes.observeForever(OneTimeObserver(changes, Observer {
            this.localChangelog = it ?: emptyList()
            this.updateChangelogState(LocalComplete)
        }))
    }

    fun loadRemoteChangelog() {
        if (this.authToken.isBlank()) {
            this.updateChangelogState(RemoteComplete(true))
        } else {
            detailsEndpoint.authToken = this.authToken
            detailsEndpoint.listener = this
            detailsEndpoint.startAsync()
        }
    }

    override fun onDataChanged(data: DfeModel) {
        val details = data as DfeDetails
        val appDetails = details.document?.appDetails
        if (appDetails != null) {
            recentChange = AppChange(appId.value!!, appDetails.versionCode, appDetails.versionString, appDetails.recentChangesHtml
                    ?: "", appDetails.uploadDate)
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
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        AppLog.e("Cannot fetch details for $appId - $error")
        this.updateChangelogState(RemoteComplete(true))
    }

    fun changeTag(tagId: Int, checked: Boolean)  {
        viewModelScope.launch {
            if (checked) {
                provide.database.appTags().delete(tagId, appId.value!!) > 0
            } else {
                AppTagsTable.Queries.insert(tagId, appId.value!!, provide.database)
            }
        }
    }

    fun watch(): Int {
        val document = this.document ?: return AppListTable.ERROR_INSERT
        val info = AppInfo(document)
        var result = AppListTable.ERROR_INSERT
        viewModelScope.launch {
            result = AppListTable.Queries.insertSafetly(info, database)
        }
        return result
    }
}