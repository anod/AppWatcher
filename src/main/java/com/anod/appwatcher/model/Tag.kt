package com.anod.appwatcher.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorInt

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class Tag(val id: Int, val name: String, @ColorInt val color: Int) : Parcelable {

    constructor(name: String) : this(-1, name, DEFAULT_COLOR)

    constructor(name: String, @ColorInt color: Int) : this(-1, name, color)

    internal constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readInt()
    )

    override fun equals(other: Any?): Boolean {
        other as? Tag ?: return false
        return when {
            id != other.id -> false
            name != other.name -> false
            color != other.color -> false
            else -> true
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.name)
        dest.writeInt(this.color)
    }

    companion object {
        const val DEFAULT_COLOR = 0xFF9E9E9E.toInt()

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
