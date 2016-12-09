package com.google.android.finsky.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class Utils {

    public static void copy(InputStream paramInputStream, OutputStream paramOutputStream)
            throws IOException {
        byte[] arrayOfByte = new byte[4096];
        try {
            while (true) {
                int i = paramInputStream.read(arrayOfByte);
                if (i == -1)
                    break;
                paramOutputStream.write(arrayOfByte, 0, i);
            }
        } finally {
            paramInputStream.close();
        }
        paramInputStream.close();
    }

    public static byte[] readBytes(InputStream paramInputStream)
            throws IOException {
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        try {
            copy(paramInputStream, localByteArrayOutputStream);
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            return arrayOfByte;
        } finally {
            localByteArrayOutputStream.close();
        }
    }

}