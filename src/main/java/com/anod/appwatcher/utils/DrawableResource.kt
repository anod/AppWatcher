package com.anod.appwatcher.utils

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * @author algavris
 * *
 * @date 06/05/2017.
 */

object DrawableResource {
    fun setTint(res: Resources, @DrawableRes drawableRes: Int, @ColorRes tint: Int, theme: Resources.Theme): Drawable {
        val d = ResourcesCompat.getDrawable(res, drawableRes, theme)
        val wrapped = DrawableCompat.wrap(d!!)
        val color = ResourcesCompat.getColor(res, tint, theme)

        DrawableCompat.setTint(wrapped, color)
        return d
    }
}
