package com.anod.appwatcher.installed

import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.preferences.Preferences
import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.api.filterDocuments
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledPackage
import info.anodsplace.playstore.AppDetailsFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ChangelogAdapter(
    private val viewModelScope: CoroutineScope,
    private val database: AppsDatabase,
    private val prefs: Preferences,
    private val dfeApi: DfeApi
) {
    private var job: Job? = null
    val changelogs = mutableMapOf<String, AppChange?>()
    val updated = MutableSharedFlow<Boolean>(0, extraBufferCapacity = 1)

    fun load(watchingPackages: List<String>, notWatchedPackages: List<InstalledPackage>) {
        AppLog.d("w: ${watchingPackages.size}, nw: ${notWatchedPackages.size}, existing ${changelogs.keys.size}")
        try {
            job?.cancel()
            job = viewModelScope.launch {
                AppLog.d("collect $watchingPackages $notWatchedPackages")
                try {
                    val hasUpdates = updateChangelog(notWatchedPackages, watchingPackages)
                    if (hasUpdates) {
                        updated.emit(true)
                    }
                } catch (e: Exception) {
                    AppLog.d("collect exception $e")
                    AppLog.e(e)
                }
            }
        } catch (e: Exception) {
            AppLog.d("exception: $e")
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
            val local = database.changelog().load(watchingPackages)
            localIds.associateByTo(changelogs, { it }, { null })
            local.associateByTo(changelogs, { it.appId }, { it })
        }
        val loadIds = packagesMap.keys.subtract(changelogs.keys)
        if (loadIds.isNotEmpty()) {
            val docIds = loadIds.map { BulkDocId(it, packagesMap.getValue(it).versionCode) }
            loadChangelogs(docIds)
        }
        val unknownIds = packagesMap.keys.subtract(changelogs.keys)
        if (unknownIds.isNotEmpty()) {
            unknownIds.associateByTo(changelogs, { it }, { null })
        }
        AppLog.d("updated $localIds, $loadIds, $unknownIds")
        return (localIds.isNotEmpty() || loadIds.isNotEmpty())
    }

    private suspend fun loadChangelogs(docIds: List<BulkDocId>) {
        if (prefs.account == null) {
            AppLog.e("No account selected", "ChangelogAdapter")
            return
        }
        try {
            val documents = dfeApi.details(docIds, includeDetails = true)
                .filterDocuments(AppDetailsFilter.hasAppDetails)
            documents.associateByTo(changelogs, { it.docId }) {
                val recentChanges = it.appDetails.recentChangesHtml?.trim() ?: ""
                AppChange(
                        appId = it.docId,
                        versionCode = it.appDetails.versionCode,
                        versionName = it.appDetails.versionString,
                        details = recentChanges,
                        uploadDate = it.appDetails.uploadDate,
                        noNewDetails = false
                )
            }
        } catch (e: Throwable) {
            AppLog.e("Fetching of bulk updates failed ${e.message ?: ""}", "ChangelogAdapter")
        }
    }
}