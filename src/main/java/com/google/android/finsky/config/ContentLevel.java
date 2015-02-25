package com.google.android.finsky.config;

import android.content.*;
import com.google.android.finsky.api.*;
import com.google.android.finsky.utils.*;
import android.accounts.*;

public class ContentLevel
{

    private static int EVERYONE = 0;
    private static int HIGH_MATURITY = 3;
    private static int LOW_MATURITY = 1;
    private static int MEDIUM_MATURITY = 2;
    private static int SHOW_ALL = 4;

    private final int mValue;

    public static ContentLevel create(Context context)
    {
        return new ContentLevel(HIGH_MATURITY);
    }

    private ContentLevel(final int mValue) {
        this.mValue = mValue;
    }

    public int getDfeValue() {
        if (this.mValue == ContentLevel.SHOW_ALL) {
            return ContentLevel.HIGH_MATURITY;
        }
        return this.mValue;
    }

    public int getValue() {
        return this.mValue;
    }
}
