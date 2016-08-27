package com.anod.appwatcher.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author algavris
 * @date 27/08/2016.
 */

public class Keyboard {

    public static void hide(View view, Context context)
    {
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }
}
