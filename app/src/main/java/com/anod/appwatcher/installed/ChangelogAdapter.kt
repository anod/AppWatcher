package com.anod.appwatcher.installed

import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.AppChange
import finsky.api.BulkDocId
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledPackage
import info.anodsplace.playstore.BulkDetailsEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

class ChangelogAdapter(
        private val viewModelScope: CoroutineScope,
        private val database: AppsDatabase,
        private val koin: Koin
) {
    private var job: Job? = null
    val changelogs = mutableMapOf<String, AppChange?>()
    val updated = MutableSharedFlow<Boolean>(0, extraBufferCapacity = 1)

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
            val local = database.changelog().load(watchingPackages)
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
        } catch (e: Throwable) {
            AppLog.e("Fetching of bulk updates failed ${e.message ?: ""}", "UpdateCheck")
            emptyList<AppChange>()
        }
    }

    private fun createEndpoint(docIds: List<BulkDocId>): BulkDetailsEndpoint {
        return koin.get { parametersOf(docIds) }
    }
}