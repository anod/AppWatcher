// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.provide
import com.anod.appwatcher.utils.SelectionState
import com.anod.appwatcher.watchlist.AppViewHolder
import info.anodsplace.framework.content.getInstalledPackages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportInstalledViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private var isImportStarted = false
    private var importManager: ImportBulkManager? = ImportBulkManager(application)
    private val selectionState = SelectionState()

    val selectedCount: Int
        get() = selectionState.count

    var selectionMode = false

    val isEmpty: Boolean
        get() = importManager!!.isEmpty

    val hasSelection: Boolean
        get() = selectionState.hasSelection

    val progress = MutableLiveData<ImportStatus>()
    val selectionChange: LiveData<SelectionState.Change> = selectionState.selectionChange

    override fun onCleared() {
        importManager = null
    }

    fun selectAll(allSelected: Boolean) {
        selectionState.selectAll(allSelected)
    }

    fun toggle(packageName: String) {
        selectionState.toggleKey(packageName)
    }

    fun import(account: Account, token: String) {
        importManager!!.reset()
        viewModelScope.launch {
            val packages = withContext(Dispatchers.Default) {
                provide.packageManager.getInstalledPackages()
                        .associateBy({ it -> it.packageName }) { it -> it.versionCode }
            }

            val size = selectionState.selectedKeys.size()
            for (i in 0 until size) {
                val packageName = selectionState.selectedKeys.keyAt(i)
                importManager!!.addPackage(packageName, packages[packageName] ?: 0)
            }

            importManager!!.start(account, token).collect { status ->
                progress.value = status
                onChanged(status)
            }
        }
    }

    fun getPackageSelection(packageName: String): AppViewHolder.Selection {
        return if (selectionMode) {
            if (selectionState.isSelected(packageName))
                AppViewHolder.Selection.Selected else AppViewHolder.Selection.NotSelected
        } else AppViewHolder.Selection.None
    }

    private fun onChanged(status: ImportStatus) {
        when (status) {
            is ImportStarted -> {
                isImportStarted = true
                status.docIds.forEach { packageName ->
                    selectionState.setStatus(packageName, importStatusProgress)
                }
            }
            is ImportProgress -> {
                status.docIds.forEach { packageName ->
                    val resultCode = status.result.get(packageName)
                    val packageStatus = if (resultCode == ImportTask.RESULT_OK) importStatusDone else importStatusError
                    selectionState.setStatus(packageName, packageStatus)
                }
            }
            is ImportFinished -> {
                isImportStarted = false
            }
        }
    }

    companion object {
        const val importStatusError = 1
        const val importStatusDone = 2
        const val importStatusProgress = 3
    }
}