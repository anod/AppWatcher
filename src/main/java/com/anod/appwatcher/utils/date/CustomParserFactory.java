package com.anod.appwatcher.utils.date;

import java.text.DateFormat;
import java.util.Locale;

/**
 * @author algavris
 * @date 12/09/2016.
 */

public class CustomParserFactory {

    public static DateFormat create(Locale locale)
    {
        if (locale.getISO3Language().equals("rus"))
        {
            return new RussianDateFormat();
        }

        return null;
    }
}
