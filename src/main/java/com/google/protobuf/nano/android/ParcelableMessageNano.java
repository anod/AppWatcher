package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.google.protobuf.nano.MessageNano;

public abstract class ParcelableMessageNano extends MessageNano
        implements Parcelable {
    public static final Parcelable.Creator<ParcelableMessageNano> CREATOR = new Parcelable.Creator() {
        public ParcelableMessageNano createFromParcel(Parcel paramAnonymousParcel) {
            return (ParcelableMessageNano) ParcelingUtil.createFromParcel(paramAnonymousParcel);
        }

        public ParcelableMessageNano[] newArray(int paramAnonymousInt) {
            return new ParcelableMessageNano[paramAnonymousInt];
        }
    };
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.protobuf.nano.android.ParcelableMessageNano
 * JD-Core Version:    0.6.2
 */