package com.anod.appwatcher.userLog

import android.content.Intent
import com.anod.appwatcher.utils.BaseFlowViewModel

data class UserLogState(
        val messages: List<Message> = emptyList()
)

sealed interface UserLogEvent {
    object OnBackNav : UserLogEvent
    object Share : UserLogEvent
}

sealed interface UserLogAction {
    class StartActivity(val intent: Intent) : UserLogAction
    object OnBackNav : UserLogAction
}

class UserLogViewModel : BaseFlowViewModel<UserLogState, UserLogEvent, UserLogAction>() {
    private val userLogger = UserLogger()

    init {
        viewState = UserLogState(
                messages = userLogger.messages
        )
    }

    override fun handleEvent(event: UserLogEvent) {
        when (event) {
            UserLogEvent.OnBackNav -> emitAction(UserLogAction.OnBackNav)
            UserLogEvent.Share -> emitAction(UserLogAction.StartActivity(intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, "AppWatcher Log")
                putExtra(Intent.EXTRA_TEXT, userLogger.content)
                type = "text/plain"
            }))
        }
    }
}