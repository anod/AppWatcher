package com.anod.appwatcher.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.anod.appwatcher.database.AppListTable
import kotlinx.parcelize.Parcelize

/**
 * @author Alex Gavrishev
 * @date 25/05/2018
 */
@Parcelize
data class Price(
        @ColumnInfo(name = AppListTable.Columns.priceText)
        val text: String,

        @ColumnInfo(name = AppListTable.Columns.priceCurrency)
        val cur: String,

        @ColumnInfo(name = AppListTable.Columns.priceMicros)
        val micros: Int?) : Parcelable {

    val isFree: Boolean
        get() = micros == 0 || micros == null
}