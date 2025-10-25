package com.anod.appwatcher.sync

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.utils.BaseFlowViewModel
import info.anodsplace.framework.content.ScreenCommonAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Immutable
data class SchedulesHistoryState(
    val schedules: ImmutableList<Schedule> = persistentListOf(),
    val dateFormat: DateFormat = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())
)

sealed interface SchedulesHistoryStateEvent {
    data object OnBackNav : SchedulesHistoryStateEvent
}

class SchedulesHistoryViewModel : BaseFlowViewModel<SchedulesHistoryState, SchedulesHistoryStateEvent, ScreenCommonAction>(), KoinComponent {
    private val database: AppsDatabase by inject()

    init {
        viewState = SchedulesHistoryState()
        viewModelScope.launch {
            database.schedules().load().collect { schedules ->
                viewState = viewState.copy(schedules = schedules.toPersistentList())
            }
        }
    }

    override fun handleEvent(event: SchedulesHistoryStateEvent) {
        when (event) {
            SchedulesHistoryStateEvent.OnBackNav -> emitAction(ScreenCommonAction.NavigateBack)
        }
    }
}