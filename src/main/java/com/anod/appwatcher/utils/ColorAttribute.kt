package com.anod.appwatcher.utils

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.util.TypedValue

/**
 * @author Alex Gavrishev
 * @date 09-Mar-18
 */
class ColorAttribute(@AttrRes private val attributeId: Int, private val context: Context, @ColorInt private val fallbackColor: Int) {

    val value: Int
        get() {
            val outValue = TypedValue()
            val wasResolved = context.theme.resolveAttribute(
                    attributeId, outValue, true)

            if (wasResolved) {
                return if (outValue.resourceId == 0) outValue.data else ContextCompat.getColor(context, outValue.resourceId)
            }
            return this.fallbackColor
        }

}