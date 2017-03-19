package com.anod.appwatcher.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author algavris
 * @date 10/03/2017.
 */

public class Tag {
    public final int id;
    public final String name;
    public final @ColorInt int color;

    public Tag(String name) {
        this(-1, name, Color.GRAY);
    }

    public Tag(int id, String name, @ColorInt int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
