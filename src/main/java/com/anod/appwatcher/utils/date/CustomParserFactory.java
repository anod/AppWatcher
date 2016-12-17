package com.anod.appwatcher.utils.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author algavris
 * @date 12/09/2016.
 */

public class CustomParserFactory {
    private static final String[] RU_SHORT_MONTHS = new String[] {
            "янв", "февр", "мар", "апр", "мая", "июн", "июл", "авг", "сент", "окт", "нояб", "дек"
    };
    private static final String[] SV_SHORT_MONTHS = new String[] {
            "jan", "feb", "mars", "apr", "maj", "juni", "juli", "aug", "sep", "okt", "nov", "dec"
    };

    public static DateFormat create(Locale locale)
    {
        String lang = locale.getLanguage();
        if (lang.equals(new Locale("ru","").getLanguage()))
        {
            return new CustomMonthDateFormat(RU_SHORT_MONTHS);
        }

        if (lang.equals(new Locale("sv","").getLanguage()))
        {
            return new CustomMonthDateFormat(SV_SHORT_MONTHS);
        }

        if (lang.equals(new Locale("de","").getLanguage()))
        {
            return new SimpleDateFormat("dd.M.yyyy", locale);
        }

        if (lang.equals(new Locale("hu","").getLanguage()))
        {
            return new SimpleDateFormat("yyyy. MMM. d.", locale);
        }

        if (locale == new Locale("en", "IN"))
        {
            return new SimpleDateFormat("dd-MMM-yyyy", locale);
        }

        if (locale == new Locale("en", "GB"))
        {
            return new SimpleDateFormat("d MMM yyyy", locale);
        }

        return null;
    }
}
