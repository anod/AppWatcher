package com.anod.appwatcher.installed

import androidx.collection.SimpleArrayMap
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import finsky.api.model.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImportInstalledTask(private val database: AppsDatabase) {

    suspend fun execute(vararg documents: Document): SimpleArrayMap<String, Int> = withContext(Dispatchers.IO) {
        val result = SimpleArrayMap<String, Int>()
        val packages = database.apps().loadPackages(false).map { it.packageName }
        for (doc in documents) {
            val info = AppInfo(doc)
            val status = addSync(info, packages, database.apps(), database)
            result.put(info.packageName, status)
        }
        return@withContext result
    }

    private suspend fun addSync(info: AppInfo, packages: List<String>, apps: AppListTable, db: AppsDatabase): Int = withContext(Dispatchers.IO) {
        if (packages.contains(info.packageName)) {
            return@withContext RESULT_OK
        }

        val existingApp = apps.loadApp(info.packageName)
        if (existingApp != null) {
            if (existingApp.status == AppInfoMetadata.STATUS_DELETED) {
                val success = apps.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_NORMAL)
                return@withContext if (success > 0) {
                    RESULT_OK
                } else {
                    ERROR_INSERT
                }
            }
            return@withContext ERROR_ALREADY_ADDED
        }

        val rowId = AppListTable.Queries.insert(info, db)
        return@withContext if (rowId > 0) RESULT_OK else ERROR_INSERT
    }

    companion object {
        const val RESULT_OK = 0
        const val ERROR_INSERT = 1
        const val ERROR_ALREADY_ADDED = 2
    }
}