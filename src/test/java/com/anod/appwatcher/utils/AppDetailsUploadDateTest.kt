package com.anod.appwatcher.utils


import android.util.Log

import com.anod.appwatcher.utils.date.CustomParserFactory

import org.junit.Test

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

import info.anodsplace.android.log.AppLog

import org.junit.Assert.assertEquals
import org.junit.Assert.fail

/**
 * @author algavris
 * *
 * @date 03/03/2017.
 */

class AppDetailsUploadDateTest {

    @Test
    @Throws(ParseException::class)
    fun extractTest() {
        AppLog.setLogLevel(Log.DEBUG)
        AppLog.LOGGER = AppLog.Logger.StdOut()

        val dates = ArrayList<DateDesc>()

        dates.add(DateDesc("es_US", "16 oct. 2016", "2016-10-16"))
        dates.add(DateDesc("es_US", "29 oct. 2016", "2016-10-29"))
        dates.add(DateDesc("es_US", "14 abr. 2017", "2017-04-14"))

        dates.add(DateDesc("es_ES", "17 mar. 2017", "2017-03-17"))
        dates.add(DateDesc("es_ES", "11 sept. 2016", "2016-09-11"))
        dates.add(DateDesc("es_ES", "28 feb. 2017", "2017-02-28"))
        dates.add(DateDesc("es_ES", "9 feb. 2017", "2017-02-09"))
        dates.add(DateDesc("es_ES", "15 dic. 2017", "2017-12-15"))
        dates.add(DateDesc("es_ES", "14 abr. 2017", "2017-04-14"))

        dates.add(DateDesc("hi_IN", "20/01/2017", "2017-01-20"))
        dates.add(DateDesc("nl_BE", "11 jul. 2016", "2016-07-11"))
        dates.add(DateDesc("en_AU", "2 Feb. 2017", "2017-02-02"))
        dates.add(DateDesc("es_MX", "1 feb. 2017", "2017-02-01"))
        dates.add(DateDesc("es_VE", "5 feb. 2017", "2017-02-05"))
        dates.add(DateDesc("fa_IR", "Feb 1, 2017", "2017-02-01"))

        //        for (int i = 0; i < 12; i++) {
        //            DateFormat df = CustomParserFactory.create(new Locale("es", "US"));
        //            System.out.println(df.format(new Date(117,i,15)));
        //        }

        val sdf = SimpleDateFormat("YYYY-MM-dd", Locale.US)
        for (date in dates) {
            try {
                val actualDate = AppDetailsUploadDate.extract(date.date, date.locale)
                assertEquals(date.locale.toString(), date.expected, sdf.format(actualDate))
            } catch (e: Exception) {
                val df = CustomParserFactory.create(date.locale)
                fail("Expected: " + df!!.format(sdf.parse(date.expected)) + ", source: " + date.date)
            }

        }
    }

    private inner class DateDesc @Throws(ParseException::class)
       constructor(locale: String, internal val date: String, internal val expected: String) {
            internal val locale: Locale = Locale(locale.substring(0, 2), locale.substring(3, 5))
    }
}
