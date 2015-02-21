package com.google.android.finsky.utils;

import com.google.protobuf.nano.*;
import android.os.*;
import android.content.*;

public class ParcelableProto<T extends MessageNano> implements Parcelable
{
    public static Parcelable.Creator<ParcelableProto<MessageNano>> CREATOR;
    private final T mPayload;
    private byte[] mSerialized;

    static {
        ParcelableProto.CREATOR = (Parcelable.Creator<ParcelableProto<MessageNano>>)new Parcelable.Creator<ParcelableProto<MessageNano>>() {
            public ParcelableProto<MessageNano> createFromParcel(final Parcel parcel) {
                final int int1 = parcel.readInt();
                if (int1 == -1) {
                    return new ParcelableProto<MessageNano>(null);
                }
                final byte[] array = new byte[int1];
                parcel.readByteArray(array);
                final String string = parcel.readString();
                try {
                    final MessageNano messageNano = (MessageNano)Class.forName(string).getConstructor((Class<?>[])null).newInstance((Object[])null);
                    messageNano.mergeFrom(CodedInputByteBufferNano.newInstance(array));
                    return new ParcelableProto<MessageNano>(messageNano);
                }
                catch (Exception ex) {
                    throw new IllegalArgumentException("Exception when unmarshalling: " + string, ex);
                }
            }

            public ParcelableProto<MessageNano>[] newArray(final int n) {
                return (ParcelableProto<MessageNano>[])new ParcelableProto[n];
            }
        };
    }

    private ParcelableProto(final T mPayload) {
        super();
        this.mSerialized = null;
        this.mPayload = mPayload;
    }

    public static <T extends MessageNano> ParcelableProto<T> forProto(final T t) {
        return new ParcelableProto<T>(t);
    }

    public static <T extends MessageNano> T getProtoFromBundle(final Bundle bundle, final String s) {
        final ParcelableProto parcelableProto = (ParcelableProto)bundle.getParcelable(s);
        if (parcelableProto != null) {
            return (T) parcelableProto.getPayload();
        }
        return null;
    }

    public static <T extends MessageNano> T getProtoFromIntent(final Intent intent, final String s) {
        final ParcelableProto parcelableProto = (ParcelableProto)intent.getParcelableExtra(s);
        if (parcelableProto != null) {
            return (T) parcelableProto.getPayload();
        }
        return null;
    }

    public static <T extends MessageNano> T getProtoFromParcel(final Parcel parcel) {
        final ParcelableProto parcelableProto = (ParcelableProto)parcel.readParcelable(ParcelableProto.class.getClassLoader());
        if (parcelableProto != null) {
            return (T) parcelableProto.getPayload();
        }
        return null;
    }

    public int describeContents() {
        return 0;
    }

    public T getPayload() {
        return this.mPayload;
    }

    public void writeToParcel(final Parcel parcel, final int n) {
        if (this.mPayload == null) {
            parcel.writeInt(-1);
            return;
        }
        if (this.mSerialized == null) {
            this.mSerialized = MessageNano.toByteArray(this.mPayload);
        }
        parcel.writeInt(this.mSerialized.length);
        parcel.writeByteArray(this.mSerialized);
        parcel.writeString(this.mPayload.getClass().getName());
    }
}
