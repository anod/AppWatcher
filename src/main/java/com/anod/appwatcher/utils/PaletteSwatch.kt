package com.anod.appwatcher.utils

import android.support.annotation.ColorInt
import android.support.v7.graphics.Palette
import android.support.v7.graphics.Target

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */

object PaletteSwatch {

    private val sDarkTargets = arrayOf(Target.DARK_VIBRANT, Target.DARK_MUTED, Target.MUTED, Target.VIBRANT)

    private val sLightTargets = arrayOf(Target.LIGHT_VIBRANT, Target.LIGHT_MUTED, Target.MUTED, Target.VIBRANT)

    fun getDark(palette: Palette, @ColorInt defaultColor: Int): Palette.Swatch {
        for (target in sDarkTargets) {
            val swatch = palette.getSwatchForTarget(target)
            if (swatch != null) {
                return swatch
            }
        }
        return Palette.Swatch(defaultColor, 0)
    }

    fun getLight(palette: Palette, @ColorInt defaultColor: Int): Palette.Swatch {
        for (target in sLightTargets) {
            val swatch = palette.getSwatchForTarget(target)
            if (swatch != null) {
                return swatch
            }
        }
        return Palette.Swatch(defaultColor, 0)
    }

}
