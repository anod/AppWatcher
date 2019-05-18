package com.anod.appwatcher.content

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.collection.SimpleArrayMap
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata

import finsky.api.model.Document
import info.anodsplace.framework.app.ApplicationContext


class AddWatchAppAsyncTask(context: ApplicationContext) {

    private val database = Application.provide(context).database

    suspend fun execute(vararg documents: Document): SimpleArrayMap<String, Int> {
        val result = SimpleArrayMap<String, Int>()
        val packages = database.apps().loadPackages(false).map { it.packageName }
        for (doc in documents) {
            val info = AppInfo(doc)
            val status = addSync(info, packages, database.apps(), database)
            result.put(info.packageName, status)
        }
        return result
    }

    private suspend fun addSync(info: AppInfo, packages: List<String>, apps: AppListTable, db: AppsDatabase): Int {
        if (packages.contains(info.packageName)) {
            return RESULT_OK
        }

        val existingApp = apps.loadApp(info.packageName)
        if (existingApp != null) {
            if (existingApp.status == AppInfoMetadata.STATUS_DELETED) {
                val success = apps.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_NORMAL)
                return if (success > 0) {
                    RESULT_OK
                } else {
                    ERROR_INSERT
                }
            }
            return ERROR_ALREADY_ADDED
        }

        val rowId = AppListTable.Queries.insert(info, db)
        return if (rowId > 0) RESULT_OK else ERROR_INSERT
    }

    companion object {
        const val RESULT_OK = 0
        const val ERROR_INSERT = 1
        const val ERROR_ALREADY_ADDED = 2
    }
}