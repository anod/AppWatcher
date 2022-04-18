// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.installed

import androidx.collection.SimpleArrayMap
import finsky.api.BulkDocId
import info.anodsplace.playstore.BulkDetailsEndpoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

sealed class ImportStatus
object ImportNotStarted : ImportStatus()
class ImportStarted(val docIds: List<String>) : ImportStatus()
class ImportProgress(val docIds: List<String>, val result: SimpleArrayMap<String, Int>) : ImportStatus()
object ImportFinished : ImportStatus()

internal class ImportBulkManager(private val koin: Koin) {

    private var listsDocIds: MutableList<MutableList<BulkDocId>?> = mutableListOf()
    private var currentBulk: Int = 0

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
                    emit(ImportStarted(docIds.map { it.packageName }))
                    val result = importDetails(docIds)
                    emit(ImportProgress(docIds.map { it.packageName }, result))
                }
            }
            emit(ImportFinished)
        }
    }

    private suspend fun importDetails(docIds: List<BulkDocId>): SimpleArrayMap<String, Int> = withContext(Main) {
        val endpoint = koin.get<BulkDetailsEndpoint> { parametersOf(docIds) }
        try {
            val model = endpoint.start()
            val docs = model.documents.toTypedArray()
            val task = koin.get<ImportInstalledTask>()
            return@withContext task.execute(*docs)
        } catch (e: Exception) {
            return@withContext SimpleArrayMap<String, Int>()
        }
    }

    companion object {
        private const val BULK_SIZE = 100
    }
}