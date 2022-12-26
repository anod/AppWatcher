package com.anod.appwatcher.installed

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.SelectionState
import com.anod.appwatcher.utils.SyncProgress
import com.anod.appwatcher.utils.getInt
import com.anod.appwatcher.utils.syncProgressFlow
import com.anod.appwatcher.watchlist.ListState
import com.anod.appwatcher.watchlist.SectionItem
import com.anod.appwatcher.watchlist.WatchListEvent
import info.anodsplace.framework.app.HingeDeviceLayout
import info.anodsplace.framework.content.getInstalledPackagesCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class InstalledListSharedState(
        val sortId: Int,
        val selectionMode: Boolean = false,
        val titleFilter: String = "",
        val listState: ListState? = null,
        val wideLayout: HingeDeviceLayout = HingeDeviceLayout(isWideLayout = false, hinge = Rect()),
        val selectedApp: App? = null,
        val importStatus: ImportStatus = ImportStatus.NotStarted,
        val selection: SelectionState = SelectionState()
)

sealed interface InstalledListSharedStateEvent {
    object OnBackPressed : InstalledListSharedStateEvent
    class SetWideLayout(val layout: HingeDeviceLayout) : InstalledListSharedStateEvent
    class UpdateSyncProgress(val syncProgress: SyncProgress) : InstalledListSharedStateEvent
    class ListEvent(val event: WatchListEvent) : InstalledListSharedStateEvent
    class FilterByTitle(val query: String) : InstalledListSharedStateEvent
    class ChangeSort(val sortId: Int) : InstalledListSharedStateEvent
    class SwitchImportMode(val selectionMode: Boolean) : InstalledListSharedStateEvent
    class SetSelection(val all: Boolean) : InstalledListSharedStateEvent
    class SelectApp(val app: App?) : InstalledListSharedStateEvent
    object Import : InstalledListSharedStateEvent
}

sealed interface InstalledListSharedStateAction {
    object OnBackPressed : InstalledListSharedStateAction
    class OpenApp(val app: App, val index: Int) : InstalledListSharedStateAction
}

class InstalledListSharedViewModel(state: SavedStateHandle) : BaseFlowViewModel<InstalledListSharedState, InstalledListSharedStateEvent, InstalledListSharedStateAction>(), KoinComponent {
    private val application: Application by inject()
    private val importManager: ImportBulkManager by inject()
    private val packageManager: PackageManager by inject()

    init {
        viewState = InstalledListSharedState(
            sortId = state.getInt("sort"),
            selectionMode = state["showAction"] ?: false
        )
        viewModelScope.launch {
            syncProgressFlow(application).collect {
                handleEvent(InstalledListSharedStateEvent.UpdateSyncProgress(syncProgress = it))
            }
        }
    }

    override fun handleEvent(event: InstalledListSharedStateEvent) {
        when (event) {
            is InstalledListSharedStateEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.layout)
            is InstalledListSharedStateEvent.UpdateSyncProgress -> {
                viewState = if (event.syncProgress.isRefreshing) {
                    viewState.copy(listState = ListState.SyncStarted)
                } else {
                    viewState.copy(listState = ListState.SyncStopped(updatesCount = event.syncProgress.updatesCount))
                }
            }
            is InstalledListSharedStateEvent.ListEvent -> handleListEvent(event.event)
            is InstalledListSharedStateEvent.ChangeSort -> {
                viewState = viewState.copy(sortId = event.sortId)
            }
            is InstalledListSharedStateEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            InstalledListSharedStateEvent.OnBackPressed -> emitAction(InstalledListSharedStateAction.OnBackPressed)
            is InstalledListSharedStateEvent.SwitchImportMode -> {
                switchImportMode(event.selectionMode)
            }
            is InstalledListSharedStateEvent.SetSelection -> {
                viewState = viewState.copy(selection = viewState.selection.selectAll(event.all))
            }
            is InstalledListSharedStateEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }

            InstalledListSharedStateEvent.Import -> import()
        }
    }

    private fun handleListEvent(listEvent: WatchListEvent) {
        when(listEvent) {
            is WatchListEvent.EmptyButton -> {}
            is WatchListEvent.FilterByTitle -> {
            }
            is WatchListEvent.ItemClick -> {
                val app = when (val item = listEvent.item) {
                    is SectionItem.App -> item.appListItem.app
                    is SectionItem.OnDevice -> item.appListItem.app
                    else -> null
                }
                if (app != null) {
                    if (viewState.selectionMode) {
                        if (app.rowId == -1) {
                            togglePackage(app.packageName)
                        }
                    } else {
                        if (viewState.wideLayout.isWideLayout) {
                            viewState = viewState.copy(selectedApp = app)
                        } else {
                            emitAction(InstalledListSharedStateAction.OpenApp(app, listEvent.index))
                        }
                    }
                }
            }
            is WatchListEvent.ItemLongClick -> {
                val app = when (val item = listEvent.item) {
                    is SectionItem.App -> item.appListItem.app
                    is SectionItem.OnDevice -> item.appListItem.app
                    else -> null
                }
                val packageName = if (app?.rowId == -1) app.packageName else null
                if (viewState.selectionMode) {
                    if (packageName != null) {
                        togglePackage(packageName)
                    }
                } else {
                    switchImportMode(true, packageName)
                }
            }
            WatchListEvent.Refresh -> { }
            WatchListEvent.Reload -> { }
        }
    }

    private fun togglePackage(packageName: String) {
        viewState = viewState.copy(selection = viewState.selection.toggleKey(packageName))
    }

    private fun switchImportMode(selectionMode: Boolean, selectedPackage: String? = null) {
        viewState = if (selectionMode) {
            if (selectedPackage != null) {
                viewState.copy(selectionMode = true, selection = viewState.selection.selectKey(selectedPackage, true))
            } else {
                viewState.copy(selectionMode = true)
            }
        } else {
            viewState.copy(selectionMode = false, selection = viewState.selection.clear())
        }
    }

    private fun import() {
        importManager.reset()
        viewModelScope.launch {
            val packages = withContext(Dispatchers.Default) {
                packageManager.getInstalledPackagesCodes()
                        .associateBy({ it.name }) { it.versionCode }
            }

            packages.forEach { (packageName, versionCode) ->
                if (viewState.selection.contains(packageName)) {
                    importManager.addPackage(packageName, versionCode)
                }
            }

            importManager.start().collect { status ->
                val selection = viewState.selection
                when (status) {
                    is ImportStatus.Started -> {
                        val newExtras = status.docIds.associateBy({ it }, { bundleOf("status" to importStatusProgress) })
                        viewState = viewState.copy(importStatus = status, selection = selection.setExtras(newExtras))
                    }
                    is ImportStatus.Progress -> {
                        val newExtras = status.docIds.associateBy({ it }) { packageName ->
                            val resultCode = status.result.get(packageName)
                            val packageStatus = if (resultCode == ImportInstalledTask.RESULT_OK) importStatusDone else importStatusError
                            bundleOf("status" to packageStatus)
                        }
                        viewState = viewState.copy(importStatus = status, selection = selection.setExtras(newExtras))
                    }
                    is ImportStatus.Finished -> {
                        viewState = viewState.copy(importStatus = status)
                    }
                    ImportStatus.NotStarted -> {
                        viewState = viewState.copy(importStatus = status)
                    }
                }
            }
        }
    }

    companion object {
        const val importStatusError = 1
        const val importStatusDone = 2
        const val importStatusProgress = 3
    }
}