// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import com.squareup.picasso.Transformation
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.graphics.PathParser
import kotlin.math.roundToInt

class AdaptiveIconTransformation(
        private val context: Context,
        private val mask: Path,
        private val layerSize: Int,
        private val cacheKey: String): Transformation {

//    constructor(context: Context, mask: Path, cacheKey: String):
//            this(context, mask, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 105f, context.resources.displayMetrics).roundToInt(), cacheKey)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)

    override fun key() = "${cacheKey.hashCode()}-${mask.hashCode()}-$layerSize"

    override fun transform(source: Bitmap): Bitmap {
        if (mask.isEmpty || source.width != source.height) {
            return source
        }

        AppLog.d("${source.width}x${source.height} [${source.hasAlpha()}] $cacheKey")

        val isTransparent = arrayOf(
                source.getPixel(0, 0) == Color.TRANSPARENT,
                source.getPixel(0, source.height - 1) == Color.TRANSPARENT,
                source.getPixel(source.width - 1, 0) == Color.TRANSPARENT,
                source.getPixel(source.width - 1, source.height - 1) == Color.TRANSPARENT
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
            val background = BitmapDrawable(context.resources, source)
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

            source.recycle()
            return resultBitmap
        }

        return source
    }

    companion object {
        private const val circlePath = "M50 0C77.6 0 100 22.4 100 50C100 77.6 77.6 100 50 100C22.4 100 0 77.6 0 50C0 22.4 22.4 0 50 0Z"
        const val MASK_SIZE = 100f

        fun getSystemDefaultMask(): String {
            val configResId = Resources.getSystem().getIdentifier("config_icon_mask", "string", "android")

            if (configResId == 0) {
                AppLog.i("No config_icon_mask")
                return circlePath
            }

            val configMask = Resources.getSystem().getString(configResId)
            AppLog.i("No configMask value: $configMask")

            if (configMask.isEmpty()) {
                return circlePath
            }

            return configMask
        }

        fun maskToPath(mask: String): Path {
            if (mask.isEmpty()) {
                return Path()
            }

            val path = PathParser.createPathFromPathData(mask)
            if (path == null) {
                AppLog.i("cannot parse configMask: $mask")
                return PathParser.createPathFromPathData(circlePath)
            }

            return path

        }
    }
}
