// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.pm.PackageManager
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.utils.SelectionState
import com.anod.appwatcher.watchlist.AppViewHolder
import info.anodsplace.framework.content.getInstalledPackagesCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class ImportInstalledViewModel(application: android.app.Application) : AndroidViewModel(application), KoinComponent {

    private var isImportStarted = false
    private var importManager: ImportBulkManager? = get()
    private val packageManager: PackageManager by inject()
    private val selectionState = SelectionState()

    val selectedCount: Int
        get() = selectionState.size

    var selectionMode = false

    val isEmpty: Boolean
        get() = importManager!!.isEmpty

    val hasSelection: Boolean
        get() = selectionState.isNotEmpty()

    val progress = MutableStateFlow<ImportStatus>(ImportNotStarted)
    val selectionChange: LiveData<SelectionState.Change> = selectionState.selectionChange

    override fun onCleared() {
        importManager = null
    }

    fun selectAll(allSelected: Boolean) {
        selectionState.selectAll(allSelected)
    }

    fun toggle(packageName: String, index: Int) {
        selectionState.toggleKey(packageName, bundleOf("index" to index))
    }

    fun clearSelection() {
        importManager!!.reset()
        selectionState.clear()
    }

    fun import() {
        importManager!!.reset()
        viewModelScope.launch {
            val packages = withContext(Dispatchers.Default) {
                packageManager.getInstalledPackagesCodes()
                        .associateBy({ it.name }) { it.versionCode }
            }

            packages.forEach { (packageName, versionCode) ->
                if (selectionState.contains(packageName)) {
                    importManager!!.addPackage(packageName, versionCode)
                }
            }

            importManager!!.start().collect { status ->
                onChanged(status)
                progress.value = status
            }
        }
    }

    fun getPackageSelection(packageName: String): AppViewHolder.Selection {
        return if (selectionMode) {
            if (selectionState.contains(packageName))
                AppViewHolder.Selection.Selected else AppViewHolder.Selection.NotSelected
        } else AppViewHolder.Selection.None
    }

    private fun onChanged(status: ImportStatus) {
        when (status) {
            is ImportStarted -> {
                isImportStarted = true
                status.docIds.forEach { packageName ->
                    selectionState.setExtra(packageName, bundleOf("status" to importStatusProgress))
                }
            }
            is ImportProgress -> {
                status.docIds.forEach { packageName ->
                    val resultCode = status.result.get(packageName)
                    val packageStatus = if (resultCode == ImportInstalledTask.RESULT_OK) importStatusDone else importStatusError
                    selectionState.setExtra(packageName, bundleOf("status" to packageStatus))
                }
            }
            is ImportFinished -> {
                isImportStarted = false
            }
            ImportNotStarted -> { }
        }
    }


    companion object {
        const val importStatusError = 1
        const val importStatusDone = 2
        const val importStatusProgress = 3
    }
}