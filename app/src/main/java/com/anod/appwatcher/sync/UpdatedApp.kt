// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.sync

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.model.AppInfo

data class UpdatedApp(
        val packageName: String,
        val versionNumber: Int,
        val title: String,
        val uploadTime: Long,
        val uploadDate: String,
        val recentChanges: String,
        val installedVersionCode: Int,
        val isNewUpdate: Boolean) {

    var noNewDetails = false

    constructor(appInfo: AppInfo, recentChanges: String, installedVersionCode: Int, isNewUpdate: Boolean)
            : this(appInfo.packageName, appInfo.versionNumber, appInfo.title, appInfo.uploadTime, appInfo.uploadDate, recentChanges, installedVersionCode, isNewUpdate)

    constructor(app: App, recentChanges: String, installedVersionCode: Int, isNewUpdate: Boolean)
            : this(app.packageName, app.versionNumber, app.title, app.uploadTime, app.uploadDate, recentChanges, installedVersionCode, isNewUpdate)

}