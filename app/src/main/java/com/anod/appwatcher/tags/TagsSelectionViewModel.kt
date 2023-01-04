package com.anod.appwatcher.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.BaseFlowViewModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

typealias TagAppItem = Pair<Tag,Boolean>

data class TagsSelectionState(
    val appId: String,
    val appTitle: String,
    val items: Map<String, Boolean> = emptyMap(),
    val tags: List<TagAppItem> = emptyList(),
    val showAddTagDialog: Boolean = false
)

sealed interface TagSelectionEvent {
    class UpdateTag(val key: String, val checked: Boolean) : TagSelectionEvent
    class AddTag(val show: Boolean, val tagId: Int = 0) : TagSelectionEvent
}

class TagsSelectionViewModel(appId: String, appTitle: String) : BaseFlowViewModel<TagsSelectionState, TagSelectionEvent, CommonActivityAction>(), KoinComponent {

    class Factory(private val appId: String, private val appTitle: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return TagsSelectionViewModel(appId, appTitle) as T
        }
    }

    private val database: AppsDatabase by inject()

    init {
        viewState = TagsSelectionState(
            appId = appId,
            appTitle = appTitle
        )

        viewModelScope.launch {
            database.tags().observe().flatMapLatest { tags ->
                database.appTags().forApp(viewState.appId).map { appTags ->
                    val appTagsList = appTags.map { it.tagId }
                    tags.map { TagAppItem(it, appTagsList.contains(it.id)) }
                }
            }.collect { tags ->
                viewState = viewState.copy(
                    tags = tags,
                    items = tags.associateBy({ "tag-${it.first.id}" }, { it.second })
                )
            }
        }
    }

    override fun handleEvent(event: TagSelectionEvent) {
        when (event) {
            is TagSelectionEvent.AddTag -> {
                viewState = viewState.copy(showAddTagDialog = event.show)
                if (!event.show && event.tagId > 0) {
                    viewModelScope.launch {
                        val tag = database.tags().loadById(event.tagId)
                        if (tag != null) {
                            AppTagsTable.Queries.insert(tag.id, viewState.appId, database)
                        }
                    }
                }
            }
            is TagSelectionEvent.UpdateTag -> {
                val tagItem = viewState.tags.firstOrNull { "tag-${it.first.id}" == event.key }
                if (tagItem != null && tagItem.second != event.checked) {
                    val tag = tagItem.first
                    viewModelScope.launch {
                        if (event.checked) {
                            AppTagsTable.Queries.insert(tag.id, viewState.appId, database)
                        } else {
                            database.appTags().delete(tag.id, viewState.appId)
                        }
                    }
                }
            }
        }
    }
}