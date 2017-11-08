package com.anod.appwatcher.framework

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import java.io.ByteArrayOutputStream
import java.io.IOException

object BitmapByteArray {
    fun flatten(bitmap: Bitmap): ByteArray? {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        val size = bitmap.width * bitmap.height * 4
        val out = ByteArrayOutputStream(size)
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            return out.toByteArray()
        } catch (e: IOException) {
            Log.w("Favorite", "Could not write icon")
            return null
        }

    }

    fun unflatten(bitmapData: ByteArray?): Bitmap? {
        var bitmap: Bitmap? = null
        if (bitmapData != null && bitmapData.isNotEmpty()) {
            bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        }
        return bitmap
    }

}
