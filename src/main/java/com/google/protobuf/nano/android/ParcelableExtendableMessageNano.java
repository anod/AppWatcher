package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.google.protobuf.nano.ExtendableMessageNano;

public abstract class ParcelableExtendableMessageNano<M extends ExtendableMessageNano<M>> extends ExtendableMessageNano<M>
        implements Parcelable {
    public static final Parcelable.Creator<ParcelableExtendableMessageNano<?>> CREATOR = new Parcelable.Creator() {
        public ParcelableExtendableMessageNano<?> createFromParcel(Parcel paramAnonymousParcel) {
            return (ParcelableExtendableMessageNano) ParcelingUtil.createFromParcel(paramAnonymousParcel);
        }

        public ParcelableExtendableMessageNano<?>[] newArray(int paramAnonymousInt) {
            return new ParcelableExtendableMessageNano[paramAnonymousInt];
        }
    };
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.protobuf.nano.android.ParcelableExtendableMessageNano
 * JD-Core Version:    0.6.2
 */