package com.anod.appwatcher.utils

import androidx.compose.material3.SnackbarDuration
import info.anodsplace.framework.content.ScreenCommonAction
import info.anodsplace.framework.content.ShowSnackbarData

data class PlainShowSnackbarData(
    val message: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val exitScreen: Boolean = false
) : ShowSnackbarData

fun showSnackbarAction(message: String, duration: SnackbarDuration = SnackbarDuration.Short, exitScreen: Boolean = false): ScreenCommonAction = ScreenCommonAction.ShowSnackbar(
    data = PlainShowSnackbarData(message, duration, exitScreen)
)