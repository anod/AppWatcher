package com.anod.appwatcher.utils

import android.support.annotation.ColorInt
import android.support.v7.graphics.Palette
import android.support.v7.graphics.Target

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */
private val sDarkTargets = arrayOf(Target.DARK_VIBRANT, Target.DARK_MUTED, Target.MUTED, Target.VIBRANT)
private val sLightTargets = arrayOf(Target.LIGHT_VIBRANT, Target.LIGHT_MUTED, Target.MUTED, Target.VIBRANT)

fun Palette.chooseDark(@ColorInt defaultColor: Int): Palette.Swatch {
    sDarkTargets
            .mapNotNull { this.getSwatchForTarget(it) }
            .forEach { return it }
    return Palette.Swatch(defaultColor, 0)
}

fun Palette.chooseLight(@ColorInt defaultColor: Int): Palette.Swatch {
    sLightTargets
            .mapNotNull { this.getSwatchForTarget(it) }
            .forEach { return it }
    return Palette.Swatch(defaultColor, 0)
}
