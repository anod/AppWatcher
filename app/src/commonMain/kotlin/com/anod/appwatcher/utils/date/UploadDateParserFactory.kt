package com.anod.appwatcher.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.*

/**
 * @author Alex Gavrishev
 * *
 * @date 12/09/2016.
 */

data class ParserLocale(val language: String, val country: String, val monthNames: MonthNames) {
    override fun toString(): String = "${language}_${country} [${monthNames.names.take(2).joinToString(",")}...]"
}

object UploadDateParserFactory {
    private val RU_SHORT_MONTHS: MonthNames = MonthNames("янв", "февр", "мар", "апр", "мая", "июн", "июл", "авг", "сент", "окт", "нояб", "дек")
    private val SV_SHORT_MONTHS: MonthNames = MonthNames("jan", "feb", "mars", "apr", "maj", "juni", "juli", "aug", "sep", "okt", "nov", "dec")
    private val ES_SHORT_MONTHS: MonthNames = MonthNames("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic")
    private val US_SHORT_MONTHS: MonthNames = MonthNames("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    private val FR_CA_SHORT_MONTHS: MonthNames = MonthNames("janv", "févr", "mars", "avr", "mai", "juin", "juill", "août", "sept", "oct", "nov", "déc")
    private val EN_SHORT_MONTHS: MonthNames = MonthNames("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec")
    private val EN_SHORT_ALT_MONTHS: MonthNames = MonthNames("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val DE_SHORT_MONTHS: MonthNames = MonthNames("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec")

    private val ru_ALL = ParserLocale("ru", "", RU_SHORT_MONTHS)
    private val sv_ALL = ParserLocale("sv", "", SV_SHORT_MONTHS)
    private val fr_CA = ParserLocale("fr", "CA", FR_CA_SHORT_MONTHS)
    // MonthNames NOT IN USE
    private val de_ALL = ParserLocale("de", "", MonthNames("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"))
    private val hu_ALL = ParserLocale("hu", "", MonthNames("jan", "febr", "márc", "ápr", "máj", "jún", "júl", "aug.", "szept", "okt", "nov", "dec"))
    private val pt_BR = ParserLocale("pt", "BR", MonthNames("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"))

    fun create(locale: ParserLocale): List<DateTimeFormat<LocalDate>> {

        val defaultParsers = listOf(
            dmy(monthNames = locale.monthNames),
            // TODO: DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
            dmy(monthNames = EN_SHORT_MONTHS),
            dmy(monthNames = ES_SHORT_MONTHS),
            dmy(monthNames = US_SHORT_MONTHS),
            mdy(monthNames = EN_SHORT_MONTHS),
            mdy(monthNames = EN_SHORT_ALT_MONTHS),
            dmy(monthNames = locale.monthNames, separator = '-'),
            dmy(monthNames = locale.monthNames, separator = '/'),
        )
        val lang = locale.language
        return when {
            lang == ru_ALL.language -> listOf(
                dmy(monthNames = RU_SHORT_MONTHS),
            ) + defaultParsers

            lang == sv_ALL.language -> listOf(
                dmy(monthNames = SV_SHORT_MONTHS),
            ) + defaultParsers

            locale == fr_CA -> listOf(
                dmy(monthNames = FR_CA_SHORT_MONTHS),
            ) + defaultParsers

            lang == de_ALL.language -> listOf(
                dmy()
            ) + defaultParsers

            lang == hu_ALL.language -> listOf(
                ymd(monthNames = locale.monthNames)
            ) + defaultParsers

            locale == pt_BR -> listOf(
                dmy(monthNames = locale.monthNames, preposition = "de"),
                dmy(monthNames = ES_SHORT_MONTHS, preposition = "de")
            ) + defaultParsers

            else -> defaultParsers
        }
    }


    // dd.M.yyyy
    private fun dmy(separator: Char = '.', suffix: String = " ") = LocalDate.Format {
        optional { char('0') }
        dayOfMonth(padding = Padding.NONE)
        char(separator)
        optional { char('0') }
        monthNumber(padding = Padding.NONE)
        char(separator)
        year()
        optional { chars(suffix) }
    }

    // dd MMM yyyy
    // dd 'de' MMM. 'de' yyyy
    private fun dmy(monthNames: MonthNames, separator: Char = ' ', preposition: String = "", suffix: String = " ") = LocalDate.Format {
        optional { char('0') }
        dayOfMonth(padding = Padding.NONE)
        char(separator)
        optional {
            chars(preposition)
            char(separator)
        }
        monthName(names = monthNames)
        optional { char('.') }
        char(separator)
        optional {
            chars(preposition)
            char(separator)
        }
        year()
        optional { chars(suffix) }
    }

    // MMM. dd, yyyy
    private fun mdy(monthNames: MonthNames, suffix: String = " ") = LocalDate.Format {
        monthName(names = monthNames)
        optional { char('.') }
        char(' ')
        optional { char('0') }
        dayOfMonth(padding = Padding.NONE)
        optional { char(',') }
        char(' ')
        year()
        optional { chars(suffix) }
    }

    // yyyy. MMM d.
    private fun ymd(monthNames: MonthNames, suffix: String = " ") = LocalDate.Format {
        year()
        optional { char('.') }
        char(' ')
        monthName(names = monthNames)
        optional { char(',') }
        optional { char('.') }
        char(' ')
        optional { char('0') }
        dayOfMonth(padding = Padding.NONE)
        optional { char('.') }
        optional { chars(suffix) }
    }
}