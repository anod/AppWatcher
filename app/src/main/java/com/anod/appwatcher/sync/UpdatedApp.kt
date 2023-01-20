// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.sync

import com.anod.appwatcher.database.entities.App

data class UpdatedApp(
    val packageName: String,
    val versionNumber: Int,
    val title: String,
    val uploadTime: Long,
    val uploadDate: String,
    val recentChanges: String,
    val installedVersionCode: Int,
    val isNewUpdate: Boolean,
    val noNewDetails: Boolean = false
) {
    constructor(appInfo: App, recentChanges: String, installedVersionCode: Int, isNewUpdate: Boolean) : this(
        appInfo.packageName,
        appInfo.versionNumber,
        appInfo.title,
        appInfo.uploadTime,
        appInfo.uploadDate,
        recentChanges,
        installedVersionCode,
        isNewUpdate
    )
}