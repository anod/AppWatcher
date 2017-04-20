package com.anod.appwatcher.utils;


import android.util.Log;

import com.anod.appwatcher.utils.date.CustomParserFactory;

import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import info.anodsplace.android.log.AppLog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author algavris
 * @date 03/03/2017.
 */

public class AppDetailsUploadDateTest {

    @Test
    public void extractTest() throws ParseException {
        AppLog.setLogLevel(Log.DEBUG);
        AppLog.LOGGER = new AppLog.Logger.StdOut();

        ArrayList<DateDesc> dates = new ArrayList<>();

        dates.add(new DateDesc("es_US", "16 oct. 2016","2016-10-16"));
        dates.add(new DateDesc("es_US", "29 oct. 2016","2016-10-29"));
        dates.add(new DateDesc("es_US", "14 abr. 2017","2017-04-14"));

        dates.add(new DateDesc("es_ES", "17 mar. 2017","2017-03-17"));
        dates.add(new DateDesc("es_ES", "11 sept. 2016","2016-09-11"));
        dates.add(new DateDesc("es_ES", "28 feb. 2017","2017-02-28"));
        dates.add(new DateDesc("es_ES", "9 feb. 2017","2017-02-09"));
        dates.add(new DateDesc("es_ES", "15 dic. 2017","2017-12-15"));
        dates.add(new DateDesc("es_ES", "14 abr. 2017","2017-04-14"));

        dates.add(new DateDesc("hi_IN", "20/01/2017","2017-01-20"));
        dates.add(new DateDesc("nl_BE", "11 jul. 2016","2016-07-11"));
        dates.add(new DateDesc("en_AU", "2 Feb. 2017","2017-02-02"));
        dates.add(new DateDesc("es_MX", "1 feb. 2017","2017-02-01"));
        dates.add(new DateDesc("es_VE", "5 feb. 2017","2017-02-05"));
        dates.add(new DateDesc("fa_IR", "Feb 1, 2017","2017-02-01"));

//        for (int i = 0; i < 12; i++) {
//            DateFormat df = CustomParserFactory.create(new Locale("es", "US"));
//            System.out.println(df.format(new Date(117,i,15)));
//        }

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd", Locale.US);
        for (DateDesc date : dates) {
            try {
                Date actualDate = AppDetailsUploadDate.extract(date.date, date.locale);
                assertEquals(date.locale.toString(), date.expected, sdf.format(actualDate));
            } catch (Exception e) {
                DateFormat df = CustomParserFactory.create(date.locale);
                fail("Expected: " + df.format(sdf.parse(date.expected)) + ", source: " + date.date);
            }
        }
    }

    private class DateDesc {
        final Locale locale;
        final String date;
        final String expected;

        private DateDesc(String locale, String date, String expected) throws ParseException {
            this.locale = new Locale(locale.substring(0,2),locale.substring(3,5));
            this.date = date;
            this.expected = expected;
        }
    }
}
