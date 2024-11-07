package com.anod.appwatcher.utils.date

import com.anod.appwatcher.utils.date.CustomMonthDateFormat.Companion.ORDER_DMY
import com.anod.appwatcher.utils.date.CustomMonthDateFormat.Companion.ORDER_MDY
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @author Alex Gavrishev
 * *
 * @date 12/09/2016.
 */

object UploadDateParserFactory {
    private val RU_SHORT_MONTHS = arrayOf("янв", "февр", "мар", "апр", "мая", "июн", "июл", "авг", "сент", "окт", "нояб", "дек")
    private val SV_SHORT_MONTHS = arrayOf("jan", "feb", "mars", "apr", "maj", "juni", "juli", "aug", "sep", "okt", "nov", "dec")
    private val ES_SHORT_MONTHS = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic")
    private val US_SHORT_MONTHS = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    private val FR_CA_SHORT_MONTHS = arrayOf("janv", "févr", "mars", "avr", "mai", "juin", "juill", "août", "sept", "oct", "nov", "déc")
    private val EN_SHORT_MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec")
    private val EN_SHORT_ALT_MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    private val ru_ALL = Locale("ru", "")
    private val sv_ALL = Locale("sv", "")
    private val fr_CA = Locale("fr", "CA")
    private val de_ALL = Locale("de", "")
    private val hu_ALL = Locale("hu", "")
    private val pt_BR = Locale("pt", "BR")

    fun create(locale: Locale): List<DateFormat> {
        val defaultParsers = listOf(
            SimpleDateFormat("d MMM. yyyy", locale),
            SimpleDateFormat("d MMM yyyy", locale),
            DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
            CustomMonthDateFormat(EN_SHORT_MONTHS, order = ORDER_DMY),
            CustomMonthDateFormat(ES_SHORT_MONTHS, order = ORDER_DMY),
            CustomMonthDateFormat(US_SHORT_MONTHS, order = ORDER_DMY),
            CustomMonthDateFormat(EN_SHORT_MONTHS, order = ORDER_MDY),
            CustomMonthDateFormat(EN_SHORT_ALT_MONTHS, order = ORDER_MDY),
            SimpleDateFormat("dd-MMM-yyyy", locale),
            SimpleDateFormat("dd/MM/yyyy", locale),
            SimpleDateFormat("MMM d, yyyy", locale),
            // en_CA
            SimpleDateFormat("MMM dd, yyyy", locale),
            SimpleDateFormat("MMM. dd, yyyy", locale)
        )
        val lang = locale.language
        return when {
            lang == ru_ALL.language -> listOf(
                CustomMonthDateFormat(RU_SHORT_MONTHS, order = ORDER_DMY),
            ) + defaultParsers

            lang == sv_ALL.language -> listOf(
                CustomMonthDateFormat(SV_SHORT_MONTHS, order = ORDER_DMY),
            ) + defaultParsers

            locale == fr_CA -> listOf(
                CustomMonthDateFormat(FR_CA_SHORT_MONTHS, order = ORDER_DMY),
            ) + defaultParsers

            lang == de_ALL.language -> listOf(
                SimpleDateFormat("dd.M.yyyy", locale),
            ) + defaultParsers

            lang == hu_ALL.language -> listOf(
                SimpleDateFormat("yyyy. MMM d.", locale),
            ) + defaultParsers

            locale == pt_BR -> listOf(
                SimpleDateFormat("dd 'de' MMM. 'de' yyyy", locale),
                SimpleDateFormat("dd 'de' MMM 'de' yyyy", locale),
                CustomMonthDateFormat(ES_SHORT_MONTHS, order = ORDER_DMY, clean = "de "),
            ) + defaultParsers

            else -> defaultParsers
        }
    }
}