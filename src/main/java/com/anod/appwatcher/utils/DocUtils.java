package com.anod.appwatcher.utils;

import android.content.Context;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.Preferences;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;

import net.hockeyapp.android.metrics.MetricsManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-23
 */
public class DocUtils {
    private static final int OFFER_TYPE = 1;
    private static final String UPLOAD_FORMAT = "MMMM d, yyyy";
    private static Object sLock = new Object();

    public static String getIconUrl(Document doc) {
        List<Common.Image> images = doc.getImages(4);
        if (images == null) {
            return null;
        }
        if (images.size() > 0) {
            return images.get(0).imageUrl;
        }
        return null;
    }

    public static Common.Offer getOffer(Document doc) {
        return doc.getOffer(OFFER_TYPE); // Type 1 ?
    }

    public static String getUrl(String packageName)
    {
        return "details?doc="+packageName;
    }

    public static long extractDate(Document doc) {
        Locale defaultLocale = Locale.getDefault();
        String uploadDate = doc.getAppDetails().uploadDate;
        return extractDate(uploadDate, defaultLocale);
    }

    private static long extractDate(String uploadDate, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        AppLog.d("Parsing: '%s' - '%s'", uploadDate, locale.toString());
        try {
            Date date = df.parse(uploadDate);
            return date.getTime();
        } catch (ParseException e) {
            AppLog.e(e);
            String format = "<UNKNOWN>";
            if (df instanceof SimpleDateFormat)
            {
                format = ((SimpleDateFormat) df).toPattern();
            }
            MetricsManagerEvent.track("ERROR_EXTRACT_DATE",
                "LOCALE", locale.toString(),
                "DEFAULT_LOCALE", Locale.getDefault().toString(),
                "FORMAT", format,
                "DATE", uploadDate);
        }

        return 0;
    }

}
