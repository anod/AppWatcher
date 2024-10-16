package com.anod.appwatcher.database.entities

import android.content.ContentValues
import com.anod.appwatcher.database.ChangelogTable

val AppChange.contentValues: ContentValues
    get() = ContentValues().apply {
        put(ChangelogTable.Columns.appId, appId)
        put(ChangelogTable.Columns.versionCode, versionCode)
        put(ChangelogTable.Columns.versionName, versionName)
        put(ChangelogTable.Columns.details, details)
        put(ChangelogTable.Columns.uploadDate, uploadDate)
        put(ChangelogTable.Columns.noNewDetails, noNewDetails)
    }