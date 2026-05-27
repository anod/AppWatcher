package com.anod.appwatcher.utils

import android.os.Build
import info.anodsplace.permissions.AppPermissions

const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"

fun isPostNotificationsPermissionRequired(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun postNotificationsPermissionRequestInput(): AppPermissions.Request.Input = AppPermissions.Request.Input.Permissions(arrayOf(POST_NOTIFICATIONS_PERMISSION))