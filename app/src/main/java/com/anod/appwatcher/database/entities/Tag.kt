package com.anod.appwatcher.database.entities

import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.TagsTable
import info.anodsplace.ktx.hashCodeOf
import kotlinx.parcelize.Parcelize

/**
 * @author Alex Gavrishev
 * @date 10/03/2017
 */
@Entity(tableName = TagsTable.table)
@Parcelize
data class Tag(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        val id: Int,
        @ColumnInfo(name = TagsTable.Columns.name)
        val name: String,
        @ColumnInfo(name = TagsTable.Columns.color)
        @ColorInt
        val color: Int) : Parcelable {

    val isEmpty: Boolean
        get() = id == 0

    @Ignore
    constructor(name: String) : this(0, name, DEFAULT_COLOR)

    @Ignore
    constructor(name: String, @ColorInt color: Int) : this(0, name, color)

    override fun equals(other: Any?): Boolean {
        if (other !is Tag) return false
        return when {
            id != other.id -> false
            name != other.name -> false
            color != other.color -> false
            else -> true
        }
    }

    override fun hashCode() = hashCodeOf(name, color)

    companion object {
        const val DEFAULT_COLOR = 0xFF2196F3.toInt()
        val empty = Tag(0, "", DEFAULT_COLOR)
    }
}