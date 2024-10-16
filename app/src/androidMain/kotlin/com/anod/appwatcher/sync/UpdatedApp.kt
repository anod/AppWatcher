// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.sync

import com.anod.appwatcher.database.entities.App

fun UpdatedApp(appInfo: App, recentChanges: String, installedVersionCode: Int, isNewUpdate: Boolean) = UpdatedApp(
    packageName = appInfo.packageName,
    versionNumber = appInfo.versionNumber,
    title = appInfo.title,
    uploadTime = appInfo.uploadTime,
    uploadDate = appInfo.uploadDate,
    recentChanges = recentChanges,
    installedVersionCode = installedVersionCode,
    isNewUpdate = isNewUpdate
)