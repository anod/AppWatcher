package com.anod.appwatcher.utils

import com.anod.appwatcher.utils.date.CustomParserFactory
import finsky.api.model.Document
import info.anodsplace.android.log.AppLog
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author alex
 * *
 * @date 2015-02-23
 */

fun Document.extractUploadDate(): Long {
    val defaultLocale = Locale.getDefault()
    val uploadDate = this.appDetails.uploadDate ?: return 0
    val date = extractUploadDate(uploadDate, defaultLocale) ?: return 0
    return date.time
}

fun extractUploadDate(uploadDate: String, locale: Locale): Date? {
    if ("" == uploadDate) {
        return null
    }
    var df = CustomParserFactory.create(locale)
    if (df != null) {
        val date = extract(uploadDate, df, locale, true)
        if (date != null) {
            return date
        }
    }

    df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    return extract(uploadDate, df, locale, false)
}

private fun extract(uploadDate: String, df: DateFormat, locale: Locale, isCustom: Boolean): Date? {
    AppLog.d("Parsing: '%s' - '%s'", uploadDate, locale.toString())
    try {
        return df.parse(uploadDate)
    } catch (e: ParseException) {
        var format = "<UNKNOWN>"
        if (df is SimpleDateFormat) {
            format = df.toPattern()
        }
        val expected = df.format(Date())

        AppLog.e(ExtractDateError(
                locale.toString(),
                Locale.getDefault().toString(),
                uploadDate,
                expected,
                format,
                e
        ))
    }

    return null
}

class ExtractDateError(
        locale: String,
        defaultLocale: String,
        actual: String,
        expected: String,
        expectedFormat: String, cause: ParseException)
    : Exception("Unparseable date: $actual, expected: $expected ($expectedFormat) [$locale], default locale: $defaultLocale", cause)

