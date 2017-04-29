package com.anod.appwatcher.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class Tag implements Parcelable {
    public static final int DEFAULT_COLOR = 0xFF9E9E9E;
    public final int id;
    public final String name;
    public final @ColorInt int color;

    public Tag(String name) {
        this(-1, name, DEFAULT_COLOR);
    }

    public Tag(int id, String name, @ColorInt int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    Tag(Parcel source) {
        this(
                source.readInt(),
                source.readString(),
                source.readInt()
        );
    }

    public static final Parcelable.Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel source) {
            return new Tag(source);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[0];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.color);
    }
}
