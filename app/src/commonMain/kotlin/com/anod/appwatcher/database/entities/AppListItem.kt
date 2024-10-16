package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.ChangelogTable

/**
 * @author Alex Gavrishev
 * @date 25/05/2018
 */
data class AppListItem(
    @Embedded
    val app: App,

    @ColumnInfo(name = ChangelogTable.Columns.details)
    val changeDetails: String?,

    @ColumnInfo(name = ChangelogTable.Columns.noNewDetails)
    val noNewDetails: Boolean,

    @ColumnInfo(name = AppListTable.Columns.recentFlag)
    val recentFlag: Boolean
)
