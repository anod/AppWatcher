package com.anod.appwatcher.tags

import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.BaseFlowViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class EditTagState(
    val tag: Tag,
    val showPickColor: Boolean = false
)

sealed interface EditTagEvent {
    class UpdateColor(@ColorInt val color: Int) : EditTagEvent
    class SaveAndDismiss(val name: String) : EditTagEvent
    object Delete : EditTagEvent
    object Dismiss : EditTagEvent
    class PickColor(val show: Boolean) : EditTagEvent
}

sealed interface EditTagAction {
    object Dismiss : EditTagAction
}

class EditTagViewModel(tag: Tag) : BaseFlowViewModel<EditTagState, EditTagEvent, EditTagAction>(), KoinComponent {

    class Factory(private val tag: Tag) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditTagViewModel(tag) as T
        }
    }

    private val database: AppsDatabase by inject()

    init {
        viewState = EditTagState(
            tag = tag
        )
    }

    override fun handleEvent(event: EditTagEvent) {
        when (event) {
            EditTagEvent.Delete -> { deleteTag(viewState.tag) }
            is EditTagEvent.SaveAndDismiss -> {
                viewState = viewState.copy(tag = Tag(viewState.tag.id,event.name, viewState.tag.color))
                saveTag(viewState.tag)
            }
            is EditTagEvent.UpdateColor -> {
                viewState = viewState.copy(tag = Tag(viewState.tag.id, viewState.tag.name, event.color))
            }
            EditTagEvent.Dismiss -> emitAction(EditTagAction.Dismiss)
            is EditTagEvent.PickColor -> {
                viewState = viewState.copy(showPickColor = event.show)
            }
        }
    }

    private fun saveTag(tag: Tag) {
        viewModelScope.launch {
            if (tag.id > 0) {
                database.tags().update(tag)
            } else {
                TagsTable.Queries.insert(tag, database).toInt()
            }
            emitAction(EditTagAction.Dismiss)
        }
    }

    private fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            TagsTable.Queries.delete(tag, database)
            emitAction(EditTagAction.Dismiss)
        }
    }
}