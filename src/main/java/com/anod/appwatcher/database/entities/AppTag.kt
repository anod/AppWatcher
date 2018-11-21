package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import android.provider.BaseColumns
import com.anod.appwatcher.database.AppTagsTable

/**
 * @author Alex Gavrishev
 * *
 * @date 27/04/2017.
 */
@Entity(tableName = AppTagsTable.table)
data class AppTag(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        val id: Int,
        @ColumnInfo(name = AppTagsTable.Columns.appId)
        val appId: String,
        @ColumnInfo(name = AppTagsTable.Columns.tagId)
        val tagId: Int) {

        @Ignore
        constructor(appId: String, tagId: Int): this(0, appId, tagId)
}
