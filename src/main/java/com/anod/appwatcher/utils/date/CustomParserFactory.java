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
    private static final String[] ES_SHORT_MONTHS = new String[] {
            "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic"
    };
    private static final String[] ES_US_SHORT_MONTHS = new String[] {
            "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"
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

        if (locale.equals(new Locale("es","ES")))
        {
            return new CustomMonthDateFormat(ES_SHORT_MONTHS);
        }

        if (locale.equals(new Locale("es","US")))
        {
            return new CustomMonthDateFormat(ES_US_SHORT_MONTHS);
        }

        if (lang.equals(new Locale("de","").getLanguage()))
        {
            return new SimpleDateFormat("dd.M.yyyy", locale);
        }

        if (lang.equals(new Locale("hu","").getLanguage()))
        {
            return new SimpleDateFormat("yyyy. MMM d.", locale);
        }

        if (lang.equals(new Locale("pl","").getLanguage()))
        {
            return new SimpleDateFormat("d MMM yyyy", locale);
        }

        if (lang.equals(new Locale("it","").getLanguage()))
        {
            return new SimpleDateFormat("d MMM yyyy", locale);
        }

        if (locale.equals(new Locale("en", "IN")))
        {
            return new SimpleDateFormat("dd-MMM-yyyy", locale);
        }

        if (locale.equals(new Locale("en", "SE"))
                || locale.equals(new Locale("en", "PH"))
                || locale.equals(new Locale("en", "AU"))
                || lang.equals(new Locale("es", "").getLanguage())
                || locale.equals(new Locale("nl", "BE"))
                )
        {
            return new SimpleDateFormat("d MMM. yyyy", locale);
        }

        if (locale.equals(new Locale("en", "GB")))
        {
            return new SimpleDateFormat("d MMM yyyy", locale);
        }

        if (locale.equals(new Locale("hi", "IN")))
        {
            return new SimpleDateFormat("dd/MM/yyyy", locale);
        }

        return null;
    }
}
