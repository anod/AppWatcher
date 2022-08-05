package com.anod.appwatcher.tags

import androidx.lifecycle.SavedStateHandle
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.prefs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

data class AppsTagScreenState(
        val tag: Tag,
        val titleFilter: String = "",
        val sortId: Int = 0,
)

sealed interface AppsTagScreenEvent {
    object OnBackPressed : AppsTagScreenEvent
    class FilterByTitle(val query: String) : AppsTagScreenEvent
}

sealed interface AppsTagScreenAction {
}

/**
 * @author Alex Gavrishev
 * @date 19/04/2016.
 */
class AppsTagViewModel(state: SavedStateHandle) : BaseFlowViewModel<AppsTagScreenState, AppsTagScreenEvent, AppsTagScreenAction>(), KoinComponent {
    private val database: AppsDatabase by inject()
    val tagAppsImport: TagAppsImport by lazy { TagAppsImport(viewState.tag, get(), get()) }

    init {
        viewState = AppsTagScreenState(
                tag = state[EXTRA_TAG] ?: Tag(0, "", 0),
                sortId = prefs.sortIndex
        )
    }

    val apps = viewStates.map { it.titleFilter }.distinctUntilChanged().flatMapLatest { titleFilter ->
        val appsTable = database.apps()
        AppListTable.Queries.loadAppList(Preferences.SORT_NAME_ASC, titleFilter, appsTable)
    }
    val tags = database.appTags().forTag(viewState.tag.id)

    override fun handleEvent(event: AppsTagScreenEvent) {
        when (event) {
            is AppsTagScreenEvent.FilterByTitle -> viewState = viewState.copy(titleFilter = event.query)
            AppsTagScreenEvent.OnBackPressed -> TODO()
        }
    }

    suspend fun import() {
        tagAppsImport.run()
    }

    companion object {
        const val EXTRA_TAG = "extra_tag"
    }

}