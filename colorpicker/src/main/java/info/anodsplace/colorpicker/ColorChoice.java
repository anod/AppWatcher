package info.anodsplace.colorpicker;

import android.content.Context;
import android.graphics.Color;

class ColorChoice {
    /**
     * Create an array of int with colors
     */
    static int[] create(Context context, int resId) {

        int[] colorChoices = null;
        String[] color_array = context.getResources().getStringArray(resId);

        if (color_array.length > 0) {
            colorChoices = new int[color_array.length];
            for (int i = 0; i < color_array.length; i++) {
                colorChoices[i] = Color.parseColor(color_array[i]);
            }
        }
        return colorChoices;
    }

}