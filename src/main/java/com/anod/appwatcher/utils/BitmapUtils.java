package com.anod.appwatcher.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtils {
    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }
    
    public static Bitmap unFlattenBitmap(byte[] bitmapData) {
    	Bitmap bitmap = null;
		if (bitmapData != null && bitmapData.length > 0) {
			bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
		}
		return bitmap;
    }
}
