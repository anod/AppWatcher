package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.DeletedTagsTable

@Entity(tableName = DeletedTagsTable.TABLE)
data class DeletedTag(
    @PrimaryKey
    @ColumnInfo(name = DeletedTagsTable.Columns.NAME)
    val name: String
)