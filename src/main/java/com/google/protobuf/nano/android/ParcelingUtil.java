package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.util.Log;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;

final class ParcelingUtil {
    static <T extends MessageNano> T createFromParcel(Parcel paramParcel) {
        String str = paramParcel.readString();
        byte[] arrayOfByte = paramParcel.createByteArray();
        T localMessageNano = null;
        try {
            localMessageNano = (T) Class.forName(str).newInstance();
            MessageNano.mergeFrom(localMessageNano, arrayOfByte);
            return localMessageNano;
        } catch (ClassNotFoundException localClassNotFoundException) {
            Log.e("ParcelingUtil", "Exception trying to create proto from parcel", localClassNotFoundException);
        } catch (IllegalAccessException localIllegalAccessException) {
            Log.e("ParcelingUtil", "Exception trying to create proto from parcel", localIllegalAccessException);
        } catch (InstantiationException localInstantiationException) {
            Log.e("ParcelingUtil", "Exception trying to create proto from parcel", localInstantiationException);
        } catch (InvalidProtocolBufferNanoException localInvalidProtocolBufferNanoException) {
            Log.e("ParcelingUtil", "Exception trying to create proto from parcel", localInvalidProtocolBufferNanoException);
        }
        return localMessageNano;
    }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.protobuf.nano.android.ParcelingUtil
 * JD-Core Version:    0.6.2
 */