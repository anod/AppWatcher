package com.anod.appwatcher.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * @author algavris
 * @date 06/05/2017.
 */

public class DrawableResource {
    public static Drawable setTint(Resources res, @DrawableRes int drawableRes, @ColorRes int tint, Resources.Theme theme) {
        Drawable d = ResourcesCompat.getDrawable(res, drawableRes, theme);
        Drawable wrapped = DrawableCompat.wrap(d);
        int color = ResourcesCompat.getColor(res, tint, theme);

        DrawableCompat.setTint(wrapped, color);
        return d;
    }
}
