package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.SelectionState
import com.anod.appwatcher.utils.getInt
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.WatchListEvent
import info.anodsplace.applog.AppLog
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
        val wideLayout: HingeDeviceLayout = HingeDeviceLayout(isWideLayout = false, hinge = Rect()),
        val selectedApp: App? = null,
        val importStatus: ImportStatus = ImportStatus.NotStarted,
        val selection: SelectionState = SelectionState(),
        val packageChanged: String = "",
        val refreshRequest: Int = 0
)

sealed interface InstalledListSharedEvent {
    object OnBackPressed : InstalledListSharedEvent
    class SetWideLayout(val layout: HingeDeviceLayout) : InstalledListSharedEvent
    class ListEvent(val event: WatchListEvent) : InstalledListSharedEvent
    class FilterByTitle(val query: String) : InstalledListSharedEvent
    class ChangeSort(val sortId: Int) : InstalledListSharedEvent
    class SwitchImportMode(val selectionMode: Boolean) : InstalledListSharedEvent
    class SetSelection(val all: Boolean) : InstalledListSharedEvent
    class SelectApp(val app: App?) : InstalledListSharedEvent
    object Import : InstalledListSharedEvent
}

sealed interface InstalledListSharedAction {
    object OnBackPressed : InstalledListSharedAction
    class StartActivity(val intent: Intent) : InstalledListSharedAction
}

class InstalledListSharedViewModel(state: SavedStateHandle) : BaseFlowViewModel<InstalledListSharedState, InstalledListSharedEvent, InstalledListSharedAction>(), KoinComponent {
    private val importManager: ImportBulkManager by inject()
    private val packageManager: PackageManager by inject()
    private val packageChanged: PackageChangedReceiver by inject()
    private val authToken: AuthTokenBlocking by inject()
    private val account: Account?
        get() = prefs.account

    init {
        viewState = InstalledListSharedState(
            sortId = state.getInt("sort"),
            selectionMode = state["showAction"] ?: false
        )
        viewModelScope.launch {
            packageChanged.observer.collect { packageChanged ->
                if (viewState.importStatus !is ImportStatus.Progress) {
                    viewState = viewState.copy(packageChanged = packageChanged)
                }
            }
        }
        checkAuthToken()
    }

    override fun handleEvent(event: InstalledListSharedEvent) {
        when (event) {
            is InstalledListSharedEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.layout)
            is InstalledListSharedEvent.ListEvent -> handleListEvent(event.event)
            is InstalledListSharedEvent.ChangeSort -> {
                viewState = viewState.copy(sortId = event.sortId)
            }
            is InstalledListSharedEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            InstalledListSharedEvent.OnBackPressed -> emitAction(InstalledListSharedAction.OnBackPressed)
            is InstalledListSharedEvent.SwitchImportMode -> {
                switchImportMode(event.selectionMode)
            }
            is InstalledListSharedEvent.SetSelection -> {
                viewState = viewState.copy(selection = viewState.selection.selectAll(event.all))
            }
            is InstalledListSharedEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }

            InstalledListSharedEvent.Import -> import()
        }
    }

    private fun handleListEvent(listEvent: WatchListEvent) {
        when(listEvent) {
            is WatchListEvent.EmptyButton -> {}
            is WatchListEvent.FilterByTitle -> {
            }
            is WatchListEvent.AppClick -> {
                if (viewState.selectionMode) {
                    if (listEvent.app.rowId == -1) {
                        togglePackage(listEvent.app.packageName)
                    }
                } else {
                    viewState = viewState.copy(selectedApp = listEvent.app)
                }
            }
            is WatchListEvent.AppLongClick -> {
                val packageName = if (listEvent.app.rowId == -1) listEvent.app.packageName else null
                if (viewState.selectionMode) {
                    if (packageName != null) {
                        togglePackage(packageName)
                    }
                } else {
                    switchImportMode(true, packageName)
                }
            }
            WatchListEvent.Refresh -> {
                checkAuthToken()
                viewState = viewState.copy(refreshRequest = viewState.refreshRequest + 1)
            }
            is WatchListEvent.SectionHeaderClick -> { }
        }
    }

    private fun checkAuthToken() {
        if (authToken.isFresh) {
            return
        }
        val account = this.account ?: return
        viewModelScope.launch {
            try {
                if (!authToken.refreshToken(account)) {
                    AppLog.e("Error retrieving token")
                }
            } catch (e: AuthTokenStartIntent) {
                emitAction(InstalledListSharedAction.StartActivity(e.intent))
            } catch (e: Exception) {
                AppLog.e("onResume", e)
            }
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