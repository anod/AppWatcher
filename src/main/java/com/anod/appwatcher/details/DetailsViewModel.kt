package com.anod.appwatcher.details

import android.accounts.Account
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.packageToApp
import finsky.api.model.DfeDetails
import finsky.api.model.DfeModel
import finsky.api.model.Document
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.framework.os.CachedBackgroundTask
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint

typealias TagMenuItem = Pair<Tag,Boolean>

class DetailsViewModel(application: Application) : AndroidViewModel(application), PlayStoreEndpoint.Listener {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    var detailsUrl = ""
    var appId = ""
    var isNewApp: Boolean = false
    var app: AppInfo? = null

    val account: Account? by lazy {
        App.provide(application).prefs.account
    }
    private val detailsEndpoint: DetailsEndpoint by lazy {
        val requestQueue = App.provide(application).requestQueue
        val deviceInfo = App.provide(application).deviceInfo
        val account = this.account ?: Account("empty", "empty")
        DetailsEndpoint(application, requestQueue, deviceInfo, account, detailsUrl)
    }
    var authToken = ""
    var localChangelog: List<AppChange> = emptyList()
    val tags = MutableLiveData<List<Tag>>()
    val tagsMenuItems = MutableLiveData<List<TagMenuItem>>()

    val changelogState = MutableLiveData<Int>()

    val document: Document?
        get() = detailsEndpoint.document

    var recentChange = AppChange(appId, 0, "", "", "")

    override fun onCleared() {
        detailsEndpoint.listener = null
    }

    fun loadChangelog() {
        this.changelogState.value = 0

        ChangesAsyncTask(ApplicationContext(getApplication()), appId, {
            this.localChangelog = it
            val state = this.changelogState.value ?: 0
            this.changelogState.value = state + 1
        }).execute()

        if (this.authToken.isEmpty()) {
            val state = this.changelogState.value ?: 0
            this.changelogState.value = state + 1
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
        val state = this.changelogState.value ?: 0
        this.changelogState.value = state + 1
    }

    override fun onErrorResponse(error: VolleyError) {
        App.log(context).error("Cannot fetch details for $appId - $error")
        val state = this.changelogState.value ?: 0
        this.changelogState.value = state + 1
    }

    fun loadApp(rowId: Int, appId: String) {
        if (rowId == -1) {
            app = context.packageManager.packageToApp(-1, appId)
            isNewApp = true
            App.log(context).info("Show details for unwatched $appId")
        } else {
            val cr = DbContentProviderClient(context)
            app = cr.queryAppRow(rowId)
            cr.close()
            isNewApp = false
            App.log(context).info("Show details for watched $appId")
        }
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
                        val appTags = cr.queryAppTags(app!!.rowId)
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