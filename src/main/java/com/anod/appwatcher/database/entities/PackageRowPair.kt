package com.anod.appwatcher.database.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey
import android.provider.BaseColumns
import com.anod.appwatcher.database.AppListTable

/**
 * @author Alex Gavrishev
 * @date 26/05/2018
 */
data class PackageRowPair(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        val rowId: Int,

        @ColumnInfo(name = AppListTable.Columns.packageName)
        val packageName: String)