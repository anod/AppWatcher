package com.anod.appwatcher.utils

import com.anod.appwatcher.utils.date.ParserLocale
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.date.UploadDateParserFactory
import com.anod.appwatcher.utils.date.parseUploadDate
import finsky.api.Document
import info.anodsplace.applog.AppLog
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toJavaLocalDate
import java.text.DateFormatSymbols
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.text.Typography.nbsp

/**
 * @author alex
 * *
 * @date 2015-02-23
 */

fun Document.extractUploadDate(cache: UploadDateParserCache, locale: Locale = Locale.getDefault()): Long {
    val uploadDate = this.appDetails.uploadDate ?: return 0
    val date = extractUploadDate(uploadDate, locale, cache) ?: return 0
    return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Locale.toParserLocale() =  ParserLocale(
    language = this.language,
    country = this.country,
    monthNames = MonthNames(DateFormatSymbols(this).shortMonths.toList()),
)

fun extractUploadDate(input: String, locale: Locale, cache: UploadDateParserCache): LocalDate? {
    if (input.isBlank()) {
        return null
    }
    return parseUploadDate(input, { locale.toParserLocale() }, cache)?.toJavaLocalDate()
}