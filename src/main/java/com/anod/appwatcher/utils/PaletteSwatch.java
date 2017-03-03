package com.anod.appwatcher.utils;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;

/**
 * @author algavris
 * @date 03/03/2017.
 */

public class PaletteSwatch {

    private static Target[] sDarkTargets = new Target[] {
            Target.DARK_VIBRANT,
            Target.DARK_MUTED,
            Target.MUTED,
            Target.VIBRANT,
    };

    private static Target[] sLightTargets = new Target[] {
            Target.LIGHT_VIBRANT,
            Target.LIGHT_MUTED,
            Target.MUTED,
            Target.VIBRANT,
    };

    public static @NonNull Palette.Swatch getDark(@NonNull Palette palette, @ColorInt int defaultColor)
    {
        for(Target target : sDarkTargets) {
            Palette.Swatch swatch = palette.getSwatchForTarget(target);
            if (swatch != null) {
                return swatch;
            }
        }
        return new Palette.Swatch(defaultColor, 0);
    }

    public static Palette.Swatch getLight(@NonNull Palette palette, @ColorInt int defaultColor)
    {
        for(Target target : sLightTargets) {
            Palette.Swatch swatch = palette.getSwatchForTarget(target);
            if (swatch != null) {
                return swatch;
            }
        }
        return new Palette.Swatch(defaultColor, 0);
    }

}
