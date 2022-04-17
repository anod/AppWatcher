// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import coil.size.Size
import coil.size.pxOrElse
import coil.transform.Transformation
import info.anodsplace.applog.AppLog
import info.anodsplace.graphics.PathParser

class AdaptiveIconTransformation(
        private val context: Context,
        private val mask: Path,
        key: String) : Transformation {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)

    override val cacheKey: String = "${key.hashCode()}-${mask.hashCode()}"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (mask.isEmpty || input.width != input.height || size.width != size.height) {
            return input
        }

        val layerSize = size.width.pxOrElse { 0 }

        val isTransparent = arrayOf(
                input.getPixel(0, 0) == Color.TRANSPARENT,
                input.getPixel(0, input.height - 1) == Color.TRANSPARENT,
                input.getPixel(input.width - 1, 0) == Color.TRANSPARENT,
                input.getPixel(input.width - 1, input.height - 1) == Color.TRANSPARENT
        ).reduce { a, b -> a && b }

        if (!isTransparent) {
            val canvas = Canvas()
            val resultBitmap = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ARGB_8888)

            val maskMatrix = Matrix()
            maskMatrix.setScale(layerSize / MASK_SIZE, layerSize / MASK_SIZE)
            val cMask = Path()
            mask.transform(maskMatrix, cMask)

            val maskBitmap = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ALPHA_8)
            val layersBitmap = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ARGB_8888)
            val background = BitmapDrawable(context.resources, input)
            background.setBounds(0, 0, layerSize, layerSize)

            paint.shader = null

            canvas.run {
                // Apply mask path to mask bitmap
                setBitmap(maskBitmap)
                drawPath(cMask, paint)

                // combine foreground and background on the layers bitmap
                setBitmap(layersBitmap)
                drawColor(Color.TRANSPARENT)

                background.draw(this)

                setBitmap(resultBitmap)
            }

            // Draw mask with layers shader on result bitmap
            paint.shader = BitmapShader(layersBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            canvas.drawBitmap(maskBitmap, 0f, 0f, paint)

            input.recycle()
            return resultBitmap
        }

        return input
    }

    companion object {
        private const val circlePath = "M50 0C77.6 0 100 22.4 100 50C100 77.6 77.6 100 50 100C22.4 100 0 77.6 0 50C0 22.4 22.4 0 50 0Z"
        const val MASK_SIZE = 100f

        fun getSystemDefaultMask(): String {
            val configResId = Resources.getSystem().getIdentifier("config_icon_mask", "string", "android")

            if (configResId == 0) {
                AppLog.d("No config_icon_mask")
                return circlePath
            }

            val configMask = Resources.getSystem().getString(configResId)
            AppLog.d("No configMask value: $configMask")

            if (configMask.isEmpty()) {
                return circlePath
            }

            return configMask
        }

        fun maskToPath(mask: String): Path {
            if (mask.isEmpty()) {
                return Path()
            }

            return try {
                val path = PathParser.createPathFromPathData(mask)
                if (path == null) {
                    AppLog.e("cannot parse configMask: $mask", "AdaptiveIcon")
                    PathParser.createPathFromPathData(circlePath)
                } else path
            } catch (e: Exception) {
                AppLog.e("error parsing configMask: $mask", "AdaptiveIcon", e)
                PathParser.createPathFromPathData(circlePath)
            }
        }
    }
}