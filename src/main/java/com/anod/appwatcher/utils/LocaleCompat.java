package com.anod.appwatcher.utils;

import android.os.Build;

import java.util.Locale;

/**
 * @author algavris
 * @date 10/09/2016.
 */

public class LocaleCompat {

    public static String toTag(Locale locale)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return locale.toLanguageTag();
        }
        String lang = locale.getISO3Language();
        String country = locale.getISO3Country();
        String variant = locale.getVariant();
        return lang + "-" + country + "-" + variant;
    }

    public static Locale fromTag(String tag)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.forLanguageTag(tag);
        }
        String[] parts = tag.split("-");
        return new Locale(parts[0],parts[1],parts[2]);
    }
}
