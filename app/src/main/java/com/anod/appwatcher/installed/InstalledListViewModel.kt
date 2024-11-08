package com.anod.appwatcher.installed

import android.content.pm.PackageManager
import android.graphics.Rect
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.CheckTokenError
import com.anod.appwatcher.accounts.CheckTokenResult
import com.anod.appwatcher.accounts.toAndroidAccount
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.SelectionState
import com.anod.appwatcher.utils.filterWithExtra
import com.anod.appwatcher.utils.getInt
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.WatchListEvent
import info.anodsplace.framework.app.HingeDeviceLayout
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.getInstalledPackagesCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Immutable
data class InstalledListState(
    val sortId: Int,
    val selectionMode: Boolean = false,
    val titleFilter: String = "",
    val wideLayout: HingeDeviceLayout = HingeDeviceLayout(isWideLayout = false, hinge = Rect()),
    val selectedApp: App? = null,
    val importStatus: ImportStatus = ImportStatus.NotStarted,
    val selection: SelectionState = SelectionState(),
    val packageChanged: String = "",
    val refreshRequest: Int = 0,
    val enablePullToRefresh: Boolean = false
)

sealed interface InstalledListEvent {
    object OnBackPressed : InstalledListEvent
    class SetWideLayout(val layout: HingeDeviceLayout) : InstalledListEvent
    class ListEvent(val event: WatchListEvent) : InstalledListEvent
    class FilterByTitle(val query: String) : InstalledListEvent
    class ChangeSort(val sortId: Int) : InstalledListEvent
    class SwitchImportMode(val selectionMode: Boolean) : InstalledListEvent
    class SetSelection(val all: Boolean) : InstalledListEvent
    class SelectApp(val app: App?) : InstalledListEvent
    class AuthTokenError(val error: CheckTokenError) : InstalledListEvent
    object Import : InstalledListEvent
    object NoAccount : InstalledListEvent
}

class InstalledListViewModel(state: SavedStateHandle) : BaseFlowViewModel<InstalledListState, InstalledListEvent, CommonActivityAction>(), KoinComponent {
    private val importManager: ImportBulkManager by inject()
    private val packageManager: PackageManager by inject()
    private val packageChanged: PackageChangedReceiver by inject()
    private val authToken: AuthTokenBlocking by inject()

    val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager))

    init {
        viewState = InstalledListState(
            sortId = state.getInt("sort"),
            selectionMode = state["showAction"] ?: false,
            enablePullToRefresh = prefs.enablePullToRefresh
        )
        viewModelScope.launch {
            packageChanged.observer.collect { packageChanged ->
                if (viewState.importStatus !is ImportStatus.Progress) {
                    viewState = viewState.copy(packageChanged = packageChanged)
                }
            }
        }
        viewModelScope.launch {
            checkAuthToken()
        }
    }

    override fun handleEvent(event: InstalledListEvent) {
        when (event) {
            is InstalledListEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.layout)
            is InstalledListEvent.ListEvent -> handleListEvent(event.event)
            is InstalledListEvent.ChangeSort -> {
                viewState = viewState.copy(sortId = event.sortId)
            }
            is InstalledListEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            InstalledListEvent.OnBackPressed -> onBackPressed()
            is InstalledListEvent.SwitchImportMode -> {
                switchImportMode(event.selectionMode)
            }

            is InstalledListEvent.SetSelection -> {
                viewState = viewState.copy(selection = viewState.selection.selectAll(event.all))
            }

            is InstalledListEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }

            InstalledListEvent.Import -> import()
            is InstalledListEvent.AuthTokenError -> {
                if (event.error is CheckTokenError.RequiresInteraction) {
                    emitAction(CommonActivityAction.StartActivity(event.error.intent))
                } else {
                    tokenErrorToast()
                }
            }

            InstalledListEvent.NoAccount -> tokenErrorToast()
        }
    }

    private fun tokenErrorToast() {
        if (networkConnection.isNetworkAvailable) {
            emitAction(
                CommonActivityAction.ShowToast(
                    resId = R.string.failed_gain_access,
                    length = Toast.LENGTH_SHORT
                )
            )
        } else {
            emitAction(
                CommonActivityAction.ShowToast(
                    resId = R.string.check_connection,
                    length = Toast.LENGTH_SHORT
                )
            )
        }
    }

    private fun onBackPressed() {
        if (viewState.wideLayout.isWideLayout) {
            if (viewState.selectedApp != null) {
                handleEvent(InstalledListEvent.SelectApp(app = null))
            } else {
                emitAction(CommonActivityAction.Finish)
            }
        } else {
            emitAction(CommonActivityAction.Finish)
        }
    }

    private fun handleListEvent(listEvent: WatchListEvent) {
        when (listEvent) {
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
                viewModelScope.launch {
                    if (checkAuthToken()) {
                        viewState = viewState.copy(refreshRequest = viewState.refreshRequest + 1)
                    }
                }
            }

            else -> {}
        }
    }

    private suspend fun checkAuthToken(): Boolean {
        val account = prefs.account?.toAndroidAccount()
        return if (account == null) {
            handleEvent(InstalledListEvent.NoAccount)
            false
        } else {
            when (val result = authToken.checkToken(account)) {
                is CheckTokenResult.Error -> {
                    handleEvent(InstalledListEvent.AuthTokenError(result.error))
                    false
                }

                is CheckTokenResult.Success -> true
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

            if (!checkAuthToken()) {
                return@launch
            }

            importManager.start().collect { status ->
                val selection = viewState.selection
                when (status) {
                    is ImportStatus.Started -> {
                        val newExtras = status.docIds.associateBy({ it }, { mapOf("status" to IMPORT_STATUS_PROGRESS) })
                        viewState = viewState.copy(importStatus = status, selection = selection.setExtras(newExtras))
                    }

                    is ImportStatus.Progress -> {
                        val statusExtras = status.docIds.associateBy({ it }) { packageName ->
                            val resultCode = status.result.get(packageName)
                            val packageStatus = if (resultCode == ImportInstalledTask.RESULT_OK) IMPORT_STATUS_DONE else IMPORT_STATUS_ERROR
                            mapOf("status" to packageStatus)
                        }
                        viewState = viewState.copy(importStatus = status, selection = selection.mergeExtras(statusExtras))
                    }
                    is ImportStatus.Finished -> {
                        val deselectKeys = selection.filterWithExtra { it["status"] == IMPORT_STATUS_DONE }
                        viewState = viewState.copy(importStatus = status, refreshRequest = viewState.refreshRequest + 1, selection = selection.selectKeys(deselectKeys, false))
                    }
                    ImportStatus.NotStarted -> {
                        viewState = viewState.copy(importStatus = status)
                    }
                }
            }
        }
    }

    companion object {
        const val IMPORT_STATUS_ERROR = "e"
        const val IMPORT_STATUS_DONE = "d"
        const val IMPORT_STATUS_PROGRESS = "p"
    }
}