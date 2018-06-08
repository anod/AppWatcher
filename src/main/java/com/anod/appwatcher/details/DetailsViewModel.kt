package com.anod.appwatcher.details

import android.accounts.Account
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.anod.appwatcher.Application
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.entities.packageToApp
import finsky.api.model.DfeDetails
import finsky.api.model.DfeModel
import finsky.api.model.Document
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.framework.os.CachedBackgroundTask
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint

typealias TagMenuItem = Pair<Tag,Boolean>

sealed class ChangelogLoadState
class LocalComplete: ChangelogLoadState()
class RemoteComplete(val error: Boolean): ChangelogLoadState()
class Complete: ChangelogLoadState()

class DetailsViewModel(application: android.app.Application) : AndroidViewModel(application), PlayStoreEndpoint.Listener {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    var detailsUrl = ""
    var appId = ""
    var rowId: Int = -1
    var isNewApp: Boolean = false

    val app: MutableLiveData<com.anod.appwatcher.database.entities.App> = MutableLiveData()

    val account: Account? by lazy {
        Application.provide(application).prefs.account
    }
    private val detailsEndpoint: DetailsEndpoint by lazy {
        val requestQueue = Application.provide(application).requestQueue
        val deviceInfo = Application.provide(application).deviceInfo
        val account = this.account ?: Account("empty", "empty")
        DetailsEndpoint(application, requestQueue, deviceInfo, account, detailsUrl)
    }
    var authToken = ""
    var localChangelog: List<AppChange> = emptyList()
    val tags = MutableLiveData<List<Tag>>()
    val tagsMenuItems = MutableLiveData<List<TagMenuItem>>()

    val changelogState = MutableLiveData<ChangelogLoadState>()

    val document: Document?
        get() = detailsEndpoint.document

    var recentChange = AppChange(appId, 0, "", "", "")

    override fun onCleared() {
        detailsEndpoint.listener = null
    }

    fun loadApp() {
        if (appId.isEmpty()) {
            app.value = null
            return
        }

        if (rowId == -1) {
            val localApp = context.packageManager.packageToApp(-1, appId)
            isNewApp = true
            Application.log(context).info("Show details for unwatched $appId")
            app.value = localApp
        } else {
            isNewApp = false
            Application.log(context).info("Show details for watched $appId")
            BackgroundTask(object : BackgroundTask.Worker<Int, App?>(rowId) {
                override fun run(param: Int): App? {
                    val appsTable = Application.provide(context).database.apps()
                    return appsTable.loadAppRow(rowId)
                }

                override fun finished(result: App?) {
                    app.value = result
                }

            }).execute()
        }
    }

    fun loadLocalChangelog() {
        if (appId.isBlank()) {
            this.updateChangelogState(LocalComplete())
            return
        }
        ChangesAsyncTask(ApplicationContext(getApplication()), appId, {
            this.localChangelog = it
            this.updateChangelogState(LocalComplete())
        }).execute()
    }

    fun loadRemoteChangelog() {
        if (this.authToken.isEmpty()) {
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
            recentChange = AppChange(appId, appDetails.versionCode, appDetails.versionString, appDetails.recentChangesHtml
                    ?: "", appDetails.uploadDate)
        }
        this.updateChangelogState(RemoteComplete(false))
    }

    private fun updateChangelogState(state: ChangelogLoadState) {
        when (state) {
            is LocalComplete -> {
                if (this.changelogState.value is RemoteComplete || this.changelogState.value is Complete) {
                    this.changelogState.value = Complete()
                } else {
                    this.changelogState.value = state
                }
            }
            is RemoteComplete -> {
                if (this.changelogState.value is LocalComplete || this.changelogState.value is Complete) {
                    this.changelogState.value = Complete()
                } else {
                    this.changelogState.value = state
                }
            }
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        Application.log(context).error("Cannot fetch details for $appId - $error")
        this.updateChangelogState(RemoteComplete(true))
    }

    fun loadTags() {
        CachedBackgroundTask("tags", object : BackgroundTask.Worker<Void?, List<Tag>>(null) {
            override fun run(param: Void?): List<Tag> {
                val cr = DbContentProviderClient(context)
                val tags = cr.queryTags()
                cr.close()
                return tags.toList()
            }

            override fun finished(result: List<Tag>) {
                this@DetailsViewModel.tags.value = result
                BackgroundTask(object : BackgroundTask.Worker<List<Tag>, List<TagMenuItem>>(result) {
                    override fun run(param: List<Tag>): List<TagMenuItem> {
                        val cr = DbContentProviderClient(context)
                        val appTags = cr.queryAppTags(rowId)
                        cr.close()
                        return param.map { TagMenuItem(it, appTags.contains(it.id)) }
                    }

                    override fun finished(result: List<TagMenuItem>) {
                        this@DetailsViewModel.tagsMenuItems.value = result
                    }
                }).execute()
            }
        }, context).execute()
    }
}