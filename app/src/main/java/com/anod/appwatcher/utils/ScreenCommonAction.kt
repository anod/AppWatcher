package com.anod.appwatcher.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import info.anodsplace.framework.content.ShowToastActionDefaults
import info.anodsplace.framework.content.StartActivityAction
import info.anodsplace.framework.content.showToast
import info.anodsplace.framework.content.startActivity

sealed interface ScreenCommonAction {
    data class StartActivity(override val intent: Intent) : ScreenCommonAction, StartActivityAction
    class ShowToast(@StringRes resId: Int = 0, text: String = "", length: Int = Toast.LENGTH_SHORT) : ShowToastActionDefaults(resId, text, length), ScreenCommonAction
    object NavigateBack : ScreenCommonAction
}

fun Context.onScreenCommonAction(action: ScreenCommonAction, navigateBack: () -> Unit) {
    when (action) {
        ScreenCommonAction.NavigateBack -> navigateBack()
        is ScreenCommonAction.ShowToast -> showToast(action)
        is ScreenCommonAction.StartActivity -> startActivity(action)
    }
}