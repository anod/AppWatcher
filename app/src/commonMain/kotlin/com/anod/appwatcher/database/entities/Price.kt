package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import com.anod.appwatcher.database.AppListTable

/**
 * @author Alex Gavrishev
 * @date 25/05/2018
 */
data class Price(
    @ColumnInfo(name = AppListTable.Columns.priceText)
    val text: String,

    @ColumnInfo(name = AppListTable.Columns.priceCurrency)
    val cur: String,

    @ColumnInfo(name = AppListTable.Columns.priceMicros)
    val micros: Int?
) {

    val isFree: Boolean
        get() = micros == 0 || micros == null
}