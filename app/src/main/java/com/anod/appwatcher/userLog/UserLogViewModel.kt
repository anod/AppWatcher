package com.anod.appwatcher.userLog

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.R
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.content.ScreenCommonAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val DIAGNOSTIC_LINE_LIMIT = 4000

@Immutable
data class UserLogState(val messages: ImmutableList<Message> = persistentListOf())

sealed interface UserLogEvent {
    data object OnBackNav : UserLogEvent
    data object Share : UserLogEvent
}

/** Shows the recorded local diagnostics when recording is enabled, otherwise the app's logcat output. */
class UserLogViewModel : BaseFlowViewModel<UserLogState, UserLogEvent, ScreenCommonAction>(), KoinComponent {
    private val diagnostics: DiagnosticRecorder by inject()
    private val userLogger = UserLogger()
    private val showsDiagnostics = prefs.isDiagnosticsEnabled

    init {
        viewState = UserLogState()
        viewModelScope.launch {
            val messages = withContext(Dispatchers.IO) {
                if (showsDiagnostics) {
                    diagnostics.recentLines(DIAGNOSTIC_LINE_LIMIT)?.let(UserLogMessage::fromDiagnosticLines)
                } else {
                    userLogger.messages
                }
            }
            if (messages == null) emitAction(ScreenCommonAction.ShowToast(resId = R.string.diagnostics_error))
            viewState = UserLogState(messages = messages.orEmpty().toPersistentList())
        }
    }

    override fun handleEvent(event: UserLogEvent) {
        when (event) {
            UserLogEvent.OnBackNav -> emitAction(ScreenCommonAction.NavigateBack)
            UserLogEvent.Share -> if (showsDiagnostics) {
                viewModelScope.launch {
                    val intent = diagnostics.shareIntent()
                    emitAction(
                        if (intent == null) ScreenCommonAction.ShowToast(resId = R.string.diagnostics_error) else ScreenCommonAction.StartActivity(intent)
                    )
                }
            } else {
                emitAction(ScreenCommonAction.StartActivity(intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, "AppWatcher Log")
                    putExtra(Intent.EXTRA_TEXT, userLogger.content)
                    type = "text/plain"
                }))
            }
        }
    }
}