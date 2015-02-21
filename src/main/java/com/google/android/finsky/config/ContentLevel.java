package com.google.android.finsky.config;

import android.content.*;
import com.google.android.finsky.api.*;
import com.google.android.finsky.utils.*;
import android.accounts.*;

public class ContentLevel
{

    private static int EVERYONE = 0;
    private static int HIGH_MATURITY = 0;
    private static int LOW_MATURITY = 0;
    private static int MEDIUM_MATURITY = 0;
    private static int SHOW_ALL = 0;

    private final int mValue;
    

    private ContentLevel(final int mValue) {
        this.mValue = mValue;
    }

    public static ContentLevel create(Context paramContext)
    {
        return new ContentLevel(HIGH_MATURITY);
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
