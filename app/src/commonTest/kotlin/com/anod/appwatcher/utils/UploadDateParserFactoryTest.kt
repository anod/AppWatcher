package com.anod.appwatcher.utils


import com.anod.appwatcher.utils.date.ParserLocale
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.date.UploadDateParserFactory
import com.anod.appwatcher.utils.date.parseUploadDate
import info.anodsplace.applog.AppLog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.test.fail

/**
 * @author Alex Gavrishev
 * *
 * @date 03/03/2017.
 */

class UploadDateParserFactoryTest {

    @Test
    fun extractTest() {
        AppLog.level = AppLog.Level.DEBUG.priority
        AppLog.logger = AppLog.Logger.StdOut()

        val dates = listOf(
            DateDesc("en_CA", "Sept 1, 2020", "2020-09-01"),
            DateDesc("en_CA", "Sep 1, 2019", "2019-09-01"),
            DateDesc("ru_RU", "8 сент. 2021 г.", "2021-09-08"),
            DateDesc("ru_RU", "22 нояб. 2021 г.", "2021-11-22"),

            DateDesc("bg_GB", "3.01.2023 г.", "2023-01-03"),
            DateDesc("bg_GB", "20.01.2023 г.", "2023-01-20"),
            DateDesc("bg_GB", "16.01.2023 г.", "2023-01-16"),

            DateDesc("nl_BE", "23 mrt. 2018", "2018-03-23"),

            DateDesc("es_VE", "17 sep. 2015", "2015-09-17"),
            DateDesc("es_VE", "5 feb. 2017", "2017-02-05"),

            DateDesc("en_SE", "30 Mar. 2018", "2018-03-30"),
            DateDesc("en_SE", "27 May. 2017", "2017-05-27"),

            DateDesc("en_RU", "28 Jun. 2017", "2017-06-28"),

            DateDesc("en_AU", "25 Apr. 2018", "2018-04-25"),
            DateDesc("en_AU", "28 Jun. 2017", "2017-06-28"),
            DateDesc("en_AU", "2 Feb. 2017", "2017-02-02"),
            DateDesc("en_AU", "7 May 2018", "2018-05-07"),
            DateDesc("en_AU", "3 May 2018", "2018-05-03"),
            DateDesc("en_AU", "30 Apr. 2018", "2018-04-30"),

            DateDesc("en_CA", "May 18, 2017", "2017-05-18"),
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

            DateDesc("es_MX", "1 feb. 2017", "2017-02-01"),
            DateDesc("es_MX", "25 abr. 2018", "2018-04-25"),

            DateDesc("fr_CA", "12 juill. 2016", "2016-07-12"),

            DateDesc("pt_BR", "5 de nov de 2017", "2017-11-05"),

            DateDesc("en_PH", "21 May 2016", "2016-05-21"),

            DateDesc("en_GB", "6 Sept 2020", "2020-09-06"),
            DateDesc("en_AU", "6 Sept 2020", "2020-09-06"),
            DateDesc("en_MY", "6 Sept 2020", "2020-09-06"),

            DateDesc("en_IN", "08-Sept-2021", "2021-09-08"),
            DateDesc("es_CO", "28 sept 2021", "2021-09-28"),
            DateDesc("es_US", "17 sept 2021", "2021-09-17"),
            DateDesc("vi_VN", "5 thg 2, 2022", "2022-02-05"),
            DateDesc("vi_VN", "29 thg 3, 2022", "2022-03-29"),
            DateDesc("en_PH", "Jan 16, 2022", "2022-01-16"),

            DateDesc("en_AU", "29 Sept 2021", "2021-09-29"),
            DateDesc("en_GB", "17 Sept 2021", "2021-09-17"),
            DateDesc("iw_IL", "7 באפר׳ 2022", "2022-04-07"),
            DateDesc("iw_IL", "11 בינו׳ 2021", "2021-01-11"),
        )

        // DateTimeFormat<LocalDate>
        val sdf = LocalDate.Formats.ISO
        for (date in dates) {
            val cache = UploadDateParserCache()
            try {
                val actualDate = parseUploadDate(date.date, { date.locale }, cache)!!
                assertEquals(date.locale.toString(), date.expected, sdf.format(actualDate))
            } catch (e: Exception) {
                val expected = LocalDate.Formats.ISO.parse(date.expected)
                val formats = UploadDateParserFactory.create(date.locale).map { df ->
                    df.format(expected)
                }.distinct()
                val formatted = "   [" + formats.joinToString("]\n   [") + "]\n"

                fail("[${date.locale}] Source: [${date.date}], Expected one of: \n$formatted")
            }
        }
    }

    private class DateDesc constructor(locale: String, val date: String, val expected: String) {
        val locale: ParserLocale = ParserLocale(locale.substring(0, 2), locale.substring(3, 5), monthNames = MonthNames.ENGLISH_ABBREVIATED )
    }
}