package com.anod.appwatcher.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.SelectionState
import com.anod.appwatcher.utils.filter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class AppsTagScreenState(
        val tag: Tag,
        val titleFilter: String = "",
        val apps: List<App> = emptyList(),
        val selection: SelectionState = SelectionState()
)

sealed interface AppsTagScreenEvent {
    object Import : AppsTagScreenEvent
    object Dismiss : AppsTagScreenEvent
    class FilterByTitle(val query: String) : AppsTagScreenEvent
    class Toggle(val appId: String) : AppsTagScreenEvent
    class SetSelection(val all: Boolean) : AppsTagScreenEvent
}

sealed interface AppsTagScreenAction {
    object Dismiss : AppsTagScreenAction
}

/**
 * @author Alex Gavrishev
 * @date 19/04/2016.
 */
class AppsTagViewModel(tag: Tag) : BaseFlowViewModel<AppsTagScreenState, AppsTagScreenEvent, AppsTagScreenAction>(), KoinComponent {

    class Factory(private val tag: Tag) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return AppsTagViewModel(tag) as T
        }
    }

    private val database: AppsDatabase by inject()

    init {
        viewState = AppsTagScreenState(tag = tag)
        viewModelScope.launch {
            viewStates
                .map { it.titleFilter }.distinctUntilChanged()
                .flatMapLatest { titleFilter ->
                    val appsTable = database.apps()
                    AppListTable.Queries.loadAppList(Preferences.SORT_NAME_ASC, titleFilter, appsTable)
                 }
                .collect { apps ->
                    viewState = viewState.copy(apps = apps.map { it.app })
                }
        }
        viewModelScope.launch {
            database.appTags().forTag(viewState.tag.id).collect { tags ->
                viewState = viewState.copy(selection = viewState.selection.selectKeys(keys = tags.map { it.appId }, select = true, overrideExisting = false))
            }
        }
    }

    override fun handleEvent(event: AppsTagScreenEvent) {
        when (event) {
            is AppsTagScreenEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            AppsTagScreenEvent.Import -> import()
            is AppsTagScreenEvent.Toggle -> {
                viewState = viewState.copy(selection = viewState.selection.toggleKey(event.appId))
            }
            AppsTagScreenEvent.Dismiss -> emitAction(AppsTagScreenAction.Dismiss)
            is AppsTagScreenEvent.SetSelection -> {
                viewState = viewState.copy(selection = viewState.selection.selectAll(event.all))
            }
        }
    }

    private fun import() {
        viewModelScope.launch {
            val appIds = if (viewState.selection.defaultSelected)
                viewState.apps.map { it.appId }
            else viewState.selection.filter(selected = true)
            AppTagsTable.Queries.assignAppsToTag(appIds, viewState.tag.id, database)
            emitAction(AppsTagScreenAction.Dismiss)
        }
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"
    }

}