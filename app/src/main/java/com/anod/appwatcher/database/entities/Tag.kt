package com.anod.appwatcher.database.entities

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.room.*
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.utils.hashCodeOf
import okhttp3.internal.toHexString

/**
 * @author Alex Gavrishev
 * @date 10/03/2017
 */
@Entity(tableName = TagsTable.table)
data class Tag(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        val id: Int,
        @ColumnInfo(name = TagsTable.Columns.name)
        val name: String,
        @ColumnInfo(name = TagsTable.Columns.color)
        @ColorInt
        val color: Int) : Parcelable {

    val darkColor: Int
        get() {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.6f
            return Color.HSVToColor(hsv)
        }

    val isLightColor: Boolean
        get() = ColorUtils.calculateLuminance(color) > 0.5

    @Ignore
    constructor(name: String) : this(0, name, DEFAULT_COLOR)

    @Ignore
    constructor(name: String, @ColorInt color: Int) : this(0, name, color)

    internal constructor(source: Parcel) : this(
            source.readInt(),
            source.readString()!!,
            source.readInt()
    )

    override fun equals(other: Any?): Boolean {
        if (other !is Tag) return false
        return when {
            id != other.id -> false
            name != other.name -> false
            color != other.color -> false
            else -> true
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.name)
        dest.writeInt(this.color)
    }

    override fun hashCode() = hashCodeOf(name, color)

    companion object {
        const val DEFAULT_COLOR = 0xFF2196F3.toInt()

        @JvmField val CREATOR: Parcelable.Creator<Tag> = object : Parcelable.Creator<Tag> {
            override fun createFromParcel(source: Parcel): Tag {
                return Tag(source)
            }

            override fun newArray(size: Int): Array<Tag> {
                return emptyArray()
            }
        }
    }
}
