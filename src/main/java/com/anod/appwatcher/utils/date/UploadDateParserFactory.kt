package com.anod.appwatcher.utils.date

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author algavris
 * *
 * @date 12/09/2016.
 */

object UploadDateParserFactory {
    private val RU_SHORT_MONTHS = arrayOf("янв", "февр", "мар", "апр", "мая", "июн", "июл", "авг", "сент", "окт", "нояб", "дек")
    private val SV_SHORT_MONTHS = arrayOf("jan", "feb", "mars", "apr", "maj", "juni", "juli", "aug", "sep", "okt", "nov", "dec")
    private val ES_SHORT_MONTHS = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic")
    private val ES_US_SHORT_MONTHS = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    private val FR_CA_SHORT_MONTHS = arrayOf("janv", "févr", "mars", "avr", "mai", "juin", "juill", "août", "sept", "oct", "nov", "déc")

    private val ru_ALL = Locale("ru", "")
    private val sv_ALL = Locale("sv", "")
    private val es_ES  = Locale("es", "ES")
    private val es_US = Locale("es", "US")
    private val fr_CA = Locale("fr", "CA")
    private val de_ALL = Locale("de", "")
    private val hu_ALL = Locale("hu", "")
    private val en_IN = Locale("en", "IN")
    private val en_CA = Locale("en", "CA")
    private val pt_BR = Locale("pt", "BR")

    fun create(locale: Locale): List<DateFormat> {
        val lang = locale.language
        if (lang == ru_ALL.language) {
            return listOf(CustomMonthDateFormat(RU_SHORT_MONTHS))
        }

        if (lang == sv_ALL.language) {
            return listOf(CustomMonthDateFormat(SV_SHORT_MONTHS))
        }

        if (locale == es_ES) {
            return listOf(CustomMonthDateFormat(ES_SHORT_MONTHS))
        }

        if (locale == es_US) {
            return listOf(CustomMonthDateFormat(ES_US_SHORT_MONTHS))
        }

        if (locale == fr_CA) {
            return listOf(CustomMonthDateFormat(FR_CA_SHORT_MONTHS))
        }

        if (lang == de_ALL.language) {
            return listOf(SimpleDateFormat("dd.M.yyyy", locale))
        }

        if (lang == hu_ALL.language) {
            return listOf(SimpleDateFormat("yyyy. MMM d.", locale))
        }

        if (locale == en_IN) {
            return listOf(SimpleDateFormat("dd-MMM-yyyy", locale))
        }

        if (locale == en_CA) {
            return listOf(SimpleDateFormat("MMM dd, yyyy", locale),
                    SimpleDateFormat("MMM. dd, yyyy", locale))
        }

        if (locale == pt_BR) {
            return listOf(SimpleDateFormat("dd 'de' MMM 'de' yyyy", locale))
        }

        if (locale == Locale("hi", "IN")) {
            return listOf(SimpleDateFormat("dd/MM/yyyy", locale))
        }

        return listOf(SimpleDateFormat("d MMM. yyyy", locale),
                SimpleDateFormat("d MMM yyyy", locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale))
    }
}
