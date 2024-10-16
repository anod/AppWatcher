package com.anod.appwatcher.sync

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
)