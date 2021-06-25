package com.anod.appwatcher.installed

import android.accounts.Account
import com.android.volley.VolleyError
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.provide
import finsky.api.BulkDocId
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledPackage
import info.anodsplace.playstore.BulkDetailsEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ChangelogAdapter(
    private val context: ApplicationContext,
    private val viewModelScope: CoroutineScope,
    private val account: Account?
) {
    private var job: Job? = null
    val changelogs = mutableMapOf<String, AppChange?>()
    val updated = MutableSharedFlow<Boolean>(0, extraBufferCapacity = 1)
    var authToken = ""

    suspend fun load(watchingPackages: List<String>, notWatchedPackages: List<InstalledPackage>) {
        AppLog.d("ChangelogAdapter.load ${watchingPackages.size}, ${notWatchedPackages.size}, existing ${changelogs.keys.size}")
        try {
            job?.cancel()
            job = viewModelScope.launch {
                AppLog.d("ChangelogAdapter collect $watchingPackages $notWatchedPackages")
                try {
                    val hasUpdates = updateChangelog(notWatchedPackages, watchingPackages)
                    if (hasUpdates) {
                        updated.emit(true)
                    }
                } catch (e: Exception) {
                    AppLog.d("ChangelogAdapter collect exception $e")
                    AppLog.e(e)
                }
            }
        } catch (e: Exception) {
            AppLog.d("ChangelogAdapter exception: $e")
            AppLog.e(e)
        }
    }

    private suspend fun updateChangelog(
        notWatchedPackages: List<InstalledPackage>,
        watchingPackages: List<String>
    ): Boolean {
        val packagesMap = notWatchedPackages.associateBy { it.name }
        val localIds = watchingPackages.subtract(changelogs.keys)
        if (localIds.isNotEmpty()) {
            val local = context.provide.database.changelog().load(watchingPackages)
            local.associateByTo(changelogs, { it.appId }, { it })
        }
        val loadIds = packagesMap.keys.subtract(changelogs.keys)
        if (loadIds.isNotEmpty()) {
            val docIds =
                loadIds.map { BulkDocId(it, packagesMap.getValue(it).versionCode) }
            loadChangelogs(docIds)
        }
        val unknownIds = packagesMap.keys.subtract(changelogs.keys)
        if (unknownIds.isNotEmpty()) {
            unknownIds.associateByTo(changelogs, { it }, { null })
        }
        AppLog.d("ChangelogAdapter updated $localIds, $loadIds, $unknownIds")
        return (localIds.isNotEmpty() || loadIds.isNotEmpty())
    }

    private suspend fun loadChangelogs(docIds: List<BulkDocId>) {
        val endpoint = createEndpoint(docIds)
        try {
            endpoint.start()
            endpoint.documents.associateByTo(changelogs, { it.docId }) {
                val recentChanges = it.appDetails.recentChangesHtml?.trim() ?: ""
                AppChange(
                    it.docId,
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