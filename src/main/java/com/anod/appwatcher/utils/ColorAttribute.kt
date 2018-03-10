package com.anod.appwatcher.utils

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.util.TypedValue

/**
 * @author algavris
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