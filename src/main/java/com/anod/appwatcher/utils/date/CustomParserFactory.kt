package com.anod.appwatcher.utils.date

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author algavris
 * *
 * @date 12/09/2016.
 */

object CustomParserFactory {
    private val RU_SHORT_MONTHS = arrayOf("янв", "февр", "мар", "апр", "мая", "июн", "июл", "авг", "сент", "окт", "нояб", "дек")
    private val SV_SHORT_MONTHS = arrayOf("jan", "feb", "mars", "apr", "maj", "juni", "juli", "aug", "sep", "okt", "nov", "dec")
    private val ES_SHORT_MONTHS = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic")
    private val ES_US_SHORT_MONTHS = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    private val FR_CA_SHORT_MONTHS = arrayOf("janv", "févr", "mars", "avr", "mai", "juin", "juill", "août", "sept", "oct", "nov", "déc")

    fun create(locale: Locale): DateFormat? {
        val lang = locale.language
        if (lang == Locale("ru", "").language) {
            return CustomMonthDateFormat(RU_SHORT_MONTHS)
        }

        if (lang == Locale("sv", "").language) {
            return CustomMonthDateFormat(SV_SHORT_MONTHS)
        }

        if (locale == Locale("es", "ES")) {
            return CustomMonthDateFormat(ES_SHORT_MONTHS)
        }

        if (locale == Locale("es", "US")) {
            return CustomMonthDateFormat(ES_US_SHORT_MONTHS)
        }

        if (locale == Locale("fr", "CA")) {
            return CustomMonthDateFormat(FR_CA_SHORT_MONTHS)
        }

        if (lang == Locale("de", "").language) {
            return SimpleDateFormat("dd.M.yyyy", locale)
        }

        if (lang == Locale("hu", "").language) {
            return SimpleDateFormat("yyyy. MMM d.", locale)
        }

        if (locale == Locale("en", "IN")) {
            return SimpleDateFormat("dd-MMM-yyyy", locale)
        }

        if (locale == Locale("en", "CA")) {
            return SimpleDateFormat("MMM. dd, yyyy", locale)
        }

        if (locale == Locale("pt", "BR")) {
            return SimpleDateFormat("dd 'de' MMM 'de' yyyy", locale)
        }

        if (locale == Locale("en", "GB")
                || locale == Locale("en", "AU")
                || lang == Locale("es", "").language
                || lang == Locale("pl", "").language
                || lang == Locale("it", "").language
                || locale == Locale("en", "SE")) {
            return SimpleDateFormat("d MMM yyyy", locale)
        }

        if (locale == Locale("en", "PH")
                || locale == Locale("nl", "BE")
                || locale == Locale("en", "RU")
                || locale == Locale("en", "NL")) {
            return SimpleDateFormat("d MMM. yyyy", locale)
        }

        if (locale == Locale("hi", "IN")) {
            return SimpleDateFormat("dd/MM/yyyy", locale)
        }

        return null
    }
}
