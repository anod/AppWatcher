package info.anodsplace.colorpicker;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author Alex Gavrishev
 * @date 14/04/2017.
 */

class ColorHex {
    final int intValue;
    final String stringValue;

    ColorHex(@ColorInt int color, boolean addAlpha) {
        String hexStr = String.format("%08X", color);
        if (!addAlpha) {
            hexStr = hexStr.substring(2);
        }
        this.stringValue = hexStr;
        this.intValue = color;
    }

    ColorHex(String hexStr, boolean addAlpha, int defColor) {
        int intValue = defColor;
        try {
            intValue = Color.parseColor("#" + hexStr);
        } catch (IllegalArgumentException e) {
            intValue = defColor;
        }
        if (!addAlpha) {
            intValue = (intValue & 0x00FFFFFF) + 0xFF000000;
        }
        this.stringValue = hexStr;
        this.intValue = intValue;
    }

}
