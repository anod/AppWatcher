// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.installed

import androidx.collection.SimpleArrayMap
import finsky.api.BulkDocId
import info.anodsplace.applog.AppLog
import info.anodsplace.playstore.BulkDetailsEndpoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

sealed interface ImportStatus {
    object NotStarted : ImportStatus
    class Started(val docIds: List<String>) : ImportStatus
    class Progress(val docIds: List<String>, val result: SimpleArrayMap<String, Int>) : ImportStatus
    object Finished : ImportStatus
}

internal class ImportBulkManager(private val koin: Koin) {

    private var listsDocIds: MutableList<MutableList<BulkDocId>?> = mutableListOf()
    private var currentBulk: Int = 0
    private val importTask: ImportInstalledTask
        get() = koin.get()

    fun reset() {
        listsDocIds = mutableListOf()
        currentBulk = 0
    }

    fun addPackage(packageName: String, versionNumber: Int) {
        var currentList: MutableList<BulkDocId>? = null
        if (listsDocIds.size > currentBulk) {
            currentList = listsDocIds[currentBulk]
        }
        if (currentList == null) {
            currentList = ArrayList()
            listsDocIds.add(currentList)
        } else if (currentList.size >= BULK_SIZE) {
            currentBulk++
            currentList = ArrayList()
            listsDocIds.add(currentList)
        }
        currentList.add(BulkDocId(packageName, versionNumber))
    }

    val isEmpty: Boolean
        get() = listsDocIds.isEmpty()

    suspend fun start(): Flow<ImportStatus> = withContext(Main) {
        currentBulk = 0
        return@withContext flow {
            for (items in listsDocIds) {
                val docIds = items ?: continue
                if (docIds.isNotEmpty()) {
                    emit(ImportStatus.Started(docIds.map { it.packageName }))
                    val result = importDetails(docIds)
                    emit(ImportStatus.Progress(docIds.map { it.packageName }, result))
                }
            }
            emit(ImportStatus.Finished)
        }
    }

    private suspend fun importDetails(docIds: List<BulkDocId>): SimpleArrayMap<String, Int> = withContext(Main) {
        val endpoint = koin.get<BulkDetailsEndpoint> { parametersOf(docIds) }
        try {
            val model = endpoint.start()
            val docs = model.documents.toTypedArray()
            return@withContext importTask.execute(*docs)
        } catch (e: Exception) {
            AppLog.e(e)
            return@withContext SimpleArrayMap<String, Int>()
        }
    }

    companion object {
        private const val BULK_SIZE = 100
    }
}