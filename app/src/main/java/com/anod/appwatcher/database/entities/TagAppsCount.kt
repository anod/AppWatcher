package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import com.anod.appwatcher.database.AppTagsTable

data class TagAppsCount(
    @ColumnInfo(name = AppTagsTable.Columns.TAGS_ID)
    val tagId: Int,
    @ColumnInfo(name = "count")
    val count: Int
)