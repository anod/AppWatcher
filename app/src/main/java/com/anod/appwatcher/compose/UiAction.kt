package com.anod.appwatcher.compose

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes

sealed class UiAction {
    object OnBackNav : UiAction()
    class Export(val uri: Uri) : UiAction()
    class Import(val uri: Uri) : UiAction()
    object OssLicenses : UiAction()
    object OpenUserLog : UiAction()
    object OpenRefreshHistory : UiAction()
    object GDriveSignIn : UiAction()
    object GDriveSignOut : UiAction()
    class GDriveErrorIntent(val intent: Intent) : UiAction()
    object Recreate : UiAction()
    object Rebirth : UiAction()
    class ShowToast(@StringRes val resId: Int = 0, val text: String = "", val length: Int = Toast.LENGTH_SHORT) : UiAction()
}