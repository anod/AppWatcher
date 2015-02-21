package com.google.android.play.dfe.utils;

import com.google.protobuf.nano.MessageNano;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class NanoProtoHelper {
    private static final Map<Class<?>, Field> sFieldCache = new HashMap();

    private static Field findField(Class<?> paramClass1, Class<?> paramClass2) {
        Field localField1 = (Field) sFieldCache.get(paramClass2);
        if (localField1 != null)
            return localField1;
        for (Field localField2 : paramClass1.getFields())
            if ((localField2.getType().equals(paramClass2)) && (Modifier.isPublic(localField2.getModifiers()))) {
                sFieldCache.put(paramClass2, localField2);
                return localField2;
            }
        throw new IllegalArgumentException("No field for " + paramClass2 + " in " + paramClass1);
    }

    public static <X extends MessageNano, Y extends MessageNano> Y getParsedResponseFromWrapper(X paramX, Class<X> paramClass, Class<Y> paramClass1) {
        try {
            Y localMessageNano = (Y) findField(paramClass, paramClass1).get(paramX);
            return localMessageNano;
        } catch (Exception localException) {
            throw new RuntimeException(localException);
        }
    }

    public static <X extends MessageNano, Y extends MessageNano> void setRequestInWrapper(X paramX, Class<X> paramClass, Y paramY, Class<Y> paramClass1) {
        try {
            findField(paramClass, paramClass1).set(paramX, paramY);
            return;
        } catch (Exception localException) {
            throw new RuntimeException(localException);
        }
    }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.play.dfe.utils.NanoProtoHelper
 * JD-Core Version:    0.6.2
 */