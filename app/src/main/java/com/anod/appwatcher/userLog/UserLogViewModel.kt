package com.anod.appwatcher.userLog

import android.content.Intent
import androidx.compose.runtime.Immutable
import com.anod.appwatcher.utils.BaseFlowViewModel
import info.anodsplace.framework.content.ScreenCommonAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Immutable
data class UserLogState(
    val messages: ImmutableList<Message> = persistentListOf()
)

sealed interface UserLogEvent {
    data object OnBackNav : UserLogEvent
    data object Share : UserLogEvent
}

class UserLogViewModel : BaseFlowViewModel<UserLogState, UserLogEvent, ScreenCommonAction>() {
    private val userLogger = UserLogger()

    init {
        viewState = UserLogState(
            messages = userLogger.messages.toPersistentList()
        )
    }

    override fun handleEvent(event: UserLogEvent) {
        when (event) {
            UserLogEvent.OnBackNav -> emitAction(ScreenCommonAction.NavigateBack)
            UserLogEvent.Share -> emitAction(ScreenCommonAction.StartActivity(intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, "AppWatcher Log")
                putExtra(Intent.EXTRA_TEXT, userLogger.content)
                type = "text/plain"
            }))
        }
    }
}