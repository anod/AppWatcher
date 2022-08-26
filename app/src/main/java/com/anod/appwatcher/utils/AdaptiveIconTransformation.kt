// Copyright (c) 2019. Alex Gavrishev
package com.anod.appwatcher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import coil.size.Size
import coil.size.pxOrElse
import coil.transform.Transformation
import info.anodsplace.graphics.AdaptiveIcon

class AdaptiveIconTransformation(context: Context, mask: Path, key: String) : Transformation {

    private val icon = AdaptiveIcon(context, mask)

    override val cacheKey: String = "${key.hashCode()}-${mask.hashCode()}"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val widthPx = size.width.pxOrElse { 0 }
        val heightPx = size.width.pxOrElse { 0 }

        return icon.transform(input, widthPx = widthPx, heightPx = heightPx)
    }
}