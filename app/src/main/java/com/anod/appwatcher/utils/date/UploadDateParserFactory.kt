package com.anod.appwatcher.utils.date

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Alex Gavrishev
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
    private val es_ES = Locale("es", "ES")
    private val es_US = Locale("es", "US")
    private val fr_CA = Locale("fr", "CA")
    private val de_ALL = Locale("de", "")
    private val hu_ALL = Locale("hu", "")
    private val en_IN = Locale("en", "IN")
    private val en_CA = Locale("en", "CA")
    private val pt_BR = Locale("pt", "BR")
    private val es_UY = Locale("es", "UY")

    fun create(locale: Locale): List<DateFormat> {
        val lang = locale.language
        return when {
            lang == ru_ALL.language -> listOf(CustomMonthDateFormat(RU_SHORT_MONTHS))
            lang == sv_ALL.language -> listOf(CustomMonthDateFormat(SV_SHORT_MONTHS))
            locale == es_ES -> listOf(CustomMonthDateFormat(ES_SHORT_MONTHS))
            locale == es_US || locale == es_UY -> listOf(CustomMonthDateFormat(ES_US_SHORT_MONTHS))
            locale == fr_CA -> listOf(CustomMonthDateFormat(FR_CA_SHORT_MONTHS))
            lang == de_ALL.language -> listOf(SimpleDateFormat("dd.M.yyyy", locale))
            lang == hu_ALL.language -> listOf(SimpleDateFormat("yyyy. MMM d.", locale))
            locale == en_IN -> listOf(SimpleDateFormat("dd-MMM-yyyy", locale))
            locale == en_CA -> listOf(
                    SimpleDateFormat("MMM dd, yyyy", locale),
                    SimpleDateFormat("MMM. dd, yyyy", locale)
            )
            locale == pt_BR -> listOf(
                    SimpleDateFormat("dd 'de' MMM. 'de' yyyy", locale),
                    SimpleDateFormat("dd 'de' MMM 'de' yyyy", locale)
            )
            locale == Locale("hi", "IN") -> listOf(SimpleDateFormat("dd/MM/yyyy", locale))
            else -> listOf(
                    SimpleDateFormat("d MMM. yyyy", locale),
                    SimpleDateFormat("d MMM yyyy", locale),
                    DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
            )
        }

    }
}
