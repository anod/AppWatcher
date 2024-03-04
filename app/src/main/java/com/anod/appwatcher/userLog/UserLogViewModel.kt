package com.anod.appwatcher.userLog

import android.content.Intent
import androidx.compose.runtime.Immutable
import info.anodsplace.framework.content.CommonActivityAction
import com.anod.appwatcher.utils.BaseFlowViewModel

@Immutable
data class UserLogState(
    val messages: List<Message> = emptyList()
)

sealed interface UserLogEvent {
    object OnBackNav : UserLogEvent
    object Share : UserLogEvent
}

class UserLogViewModel : BaseFlowViewModel<UserLogState, UserLogEvent, CommonActivityAction>() {
    private val userLogger = UserLogger()

    init {
        viewState = UserLogState(
                messages = userLogger.messages
        )
    }

    override fun handleEvent(event: UserLogEvent) {
        when (event) {
            UserLogEvent.OnBackNav -> emitAction(CommonActivityAction.Finish)
            UserLogEvent.Share -> emitAction(CommonActivityAction.StartActivity(intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, "AppWatcher Log")
                putExtra(Intent.EXTRA_TEXT, userLogger.content)
                type = "text/plain"
            }))
        }
    }
}