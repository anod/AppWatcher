package com.anod.appwatcher.installed

import android.accounts.Account
import com.android.volley.VolleyError
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.provide
import finsky.api.BulkDocId
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledPackage
import info.anodsplace.playstore.BulkDetailsEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

typealias Request = Pair<List<String>, List<InstalledPackage>>

class ChangelogAdapter(
        private val context: ApplicationContext,
        private val viewModelScope: CoroutineScope
) {
    private val request = MutableSharedFlow<Request>()
    private var job: Job? = null
    val changelogs = mutableMapOf<String, AppChange>()
    val updated = MutableSharedFlow<Boolean>(0)
    val account: Account? = context.provide.prefs.account
    var authToken = ""
        set(value) {
            field = value
            if (value.isNotEmpty() && account != null) {
                subscribeForChangelogRequests()
            }
        }


    suspend fun load(watchingPackages: List<String>, notWatchedPackages: List<InstalledPackage>) {
        try {
            request.emit(Pair(watchingPackages, notWatchedPackages))
        } catch (e: Exception) {
            AppLog.e(e)
        }
    }

    private fun subscribeForChangelogRequests() {
        job?.cancel()
        job = viewModelScope.launch {
            request.collect { request ->
                try {
                    val packagesMap = request.second.associateBy { it.name }
                    val localIds = request.first.subtract(changelogs.keys)
                    if (localIds.isNotEmpty()) {
                        val local = context.provide.database.changelog().load(request.first)
                        local.associateByTo(changelogs, { it.appId }, { it })
                    }
                    val loadIds = packagesMap.keys.subtract(changelogs.keys)
                    if (loadIds.isNotEmpty()) {
                        val docIds = loadIds.map { BulkDocId(it, packagesMap.getValue(it).versionCode) }
                        loadChangelogs(docIds)
                    }
                    updated.emit(true)
                } catch (e: Exception) {
                    AppLog.e(e)
                }
            }
        }
    }

    private suspend fun loadChangelogs(docIds: List<BulkDocId>) {
        val endpoint = createEndpoint(docIds)
        try {
            endpoint.start()
            endpoint.documents.associateByTo(changelogs, { it.docId }) {
                val recentChanges = it.appDetails.recentChangesHtml?.trim() ?: ""
                AppChange(it.docId,
                        it.appDetails.versionCode, it.appDetails.versionString,
                        recentChanges, it.appDetails.uploadDate, false
                )
            }
        } catch (e: VolleyError) {
            AppLog.e("Fetching of bulk updates failed ${e.message ?: ""}", "UpdateCheck")
            emptyList<AppChange>()
        }
    }

    private fun createEndpoint(docIds: List<BulkDocId>): BulkDetailsEndpoint {
        return BulkDetailsEndpoint(
                context.actual,
                context.provide.requestQueue, context.provide.deviceInfo,
                account!!, docIds
        ).also { it.authToken = authToken }
    }

}