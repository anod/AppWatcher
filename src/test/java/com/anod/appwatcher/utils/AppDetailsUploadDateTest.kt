package com.anod.appwatcher.utils


import android.util.Log

import com.anod.appwatcher.utils.date.CustomParserFactory

import org.junit.Test

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

import info.anodsplace.framework.AppLog

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

        val dates = listOf(
                DateDesc("en_RU", "28 Jun. 2017", "2017-06-28"),

                DateDesc("en_AU", "28 Jun 2017", "2017-06-28"),
                DateDesc("en_CA", "Nov. 18, 2017", "2017-11-18"),

                DateDesc("es_US", "16 oct. 2016", "2016-10-16"),
                DateDesc("es_US", "29 oct. 2016", "2016-10-29"),
                DateDesc("es_US", "14 abr. 2017", "2017-04-14"),

                DateDesc("es_ES", "17 mar. 2017", "2017-03-17"),
                DateDesc("es_ES", "11 sept. 2016", "2016-09-11"),
                DateDesc("es_ES", "28 feb. 2017", "2017-02-28"),
                DateDesc("es_ES", "9 feb. 2017", "2017-02-09"),
                DateDesc("es_ES", "15 dic. 2017", "2017-12-15"),
                DateDesc("es_ES", "14 abr. 2017", "2017-04-14"),

                DateDesc("hi_IN", "20/01/2017", "2017-01-20"),
                DateDesc("nl_BE", "11 jul. 2016", "2016-07-11"),
                DateDesc("en_AU", "2 Feb 2017", "2017-02-02"),
                DateDesc("es_MX", "1 feb. 2017", "2017-02-01"),
                DateDesc("es_VE", "5 feb. 2017", "2017-02-05"),
                DateDesc("es_AR", "4 dic. 2017", "2017-12-04"),
                DateDesc("en_SE", "27 May 2017", "2017-05-27"),

                DateDesc("fa_IR", "Feb 1, 2017", "2017-02-01"),

                DateDesc("fr_CA", "12 juill. 2016", "2016-07-12"),
                DateDesc("pt_BR", "5 de nov de 2017", "2017-11-05")
        )

        //        for (int i = 0; i < 12; i++) {
        //            DateFormat df = CustomParserFactory.create(new Locale("es", "US"));
        //            System.out.println(df.format(new Date(117,i,15)));
        //        }

        val sdf = SimpleDateFormat("YYYY-MM-dd", Locale.US)
        for (date in dates) {
            try {
                val actualDate = extractUploadDate(date.date, date.locale)
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
