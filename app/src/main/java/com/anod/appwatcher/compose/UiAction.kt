package com.anod.appwatcher.compose

import android.content.Intent
import android.net.Uri

sealed class UiAction {
    object OnBackNav : UiAction()
    class Export(val uri: Uri) : UiAction()
    class Import(val uri: Uri) : UiAction()
    object OssLicenses : UiAction()
    object OpenUserLog : UiAction()
    object OpenRefreshHistory : UiAction()
    object GDriveSignIn : UiAction()
    class GDriveErrorIntent(val intent: Intent) : UiAction()
    object Recreate : UiAction()
}