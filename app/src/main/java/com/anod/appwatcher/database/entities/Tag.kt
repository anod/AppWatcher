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
import kotlinx.serialization.Serializable

/**
 * @author Alex Gavrishev
 * @date 10/03/2017
 */
@Entity(tableName = TagsTable.TABLE)
@Parcelize
@Serializable
data class Tag(
    @PrimaryKey
    @ColumnInfo(name = BaseColumns._ID)
    val id: Int,
    @ColumnInfo(name = TagsTable.Columns.NAME)
    val name: String,
    @ColumnInfo(name = TagsTable.Columns.COLOR)
    @param:ColorInt
    val color: Int,
    @ColumnInfo(name = TagsTable.Columns.STATUS, defaultValue = "0")
    val status: Int = STATUS_NORMAL
) : Parcelable {

    val isEmpty: Boolean
        get() = id == 0

    @Ignore
    constructor(name: String) : this(0, name, DEFAULT_COLOR, STATUS_NORMAL)

    @Ignore
    constructor(name: String, @ColorInt color: Int) : this(0, name, color, STATUS_NORMAL)

    override fun equals(other: Any?): Boolean {
        if (other !is Tag) return false
        return when {
            id != other.id -> false
            name != other.name -> false
            color != other.color -> false
            status != other.status -> false
            else -> true
        }
    }

    override fun hashCode() = hashCodeOf(id, name, color, status)

    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_DELETED = 1
        const val DEFAULT_COLOR = 0xFF2196F3.toInt()
        val empty = Tag(0, "", DEFAULT_COLOR, STATUS_NORMAL)
    }
}