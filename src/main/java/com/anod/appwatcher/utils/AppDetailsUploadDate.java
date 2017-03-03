package com.anod.appwatcher.utils;

import android.text.TextUtils;

import com.anod.appwatcher.utils.date.CustomParserFactory;
import com.google.android.finsky.api.model.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-23
 */
public class AppDetailsUploadDate {

    public static long extract(Document doc) {
        Locale defaultLocale = Locale.getDefault();
        String uploadDate = doc.getAppDetails().uploadDate;
        Date date = extract(uploadDate, defaultLocale);
        if (date == null)
        {
            return 0;
        }
        return date.getTime();
    }

    static Date extract(String uploadDate, Locale locale) {
        if ("".equals(uploadDate))
        {
            return null;
        }
        DateFormat df = CustomParserFactory.create(locale);
        if (df != null) {
            Date date = extract(uploadDate, df, locale, true);
            if (date != null)
            {
                return date;
            }
        }

        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        return extract(uploadDate, df, locale, false);
    }

    private static Date extract(String uploadDate, DateFormat df, Locale locale, boolean isCustom)
    {
        AppLog.d("Parsing: '%s' - '%s'", uploadDate, locale.toString());
        try {
            return df.parse(uploadDate);
        } catch (ParseException e) {
            AppLog.e(e);
            String format = "<UNKNOWN>";
            if (df instanceof SimpleDateFormat)
            {
                format = ((SimpleDateFormat) df).toPattern();
            }
            String expected = df.format(new Date());
            MetricsManagerEvent.track("ERROR_EXTRACT_DATE",
                    "LOCALE", locale.toString(),
                    "DEFAULT_LOCALE", Locale.getDefault().toString(),
                    "ACTUAL", uploadDate,
                    "EXPECTED", expected,
                    "EXPECTED_FORMAT", format,
                    "CUSTOM", isCustom ? "YES" : "NO");
        }

        return null;

    }

}
