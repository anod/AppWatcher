package com.anod.appwatcher.utils;

import android.os.Build;
import android.view.View;

/**
 * @author alex
 * @date 2015-03-05
 */
public class UIUtils {

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }
}
