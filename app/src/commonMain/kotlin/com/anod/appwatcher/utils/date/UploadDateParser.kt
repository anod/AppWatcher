package com.anod.appwatcher.utils.date

import info.anodsplace.applog.AppLog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat

class UploadDateParserCache {
    var parser: DateTimeFormat<LocalDate>? = null
}

fun parseUploadDate(input: String, localeFactory: () -> ParserLocale, cache: UploadDateParserCache): LocalDate? {
    val uploadDate = input
        .replace('\u202F', ' ')
        .replace(Typography.nbsp, ' ')

    val parser = cache.parser
    if (parser != null) {
        try {
            val date = parser.parse(uploadDate)
            return date
        } catch (e: IllegalArgumentException) {
            val locale = localeFactory()
            AppLog.e("Cannot parse '$uploadDate' for '$locale' with cached parser")
        }
    }

    val locale = localeFactory()
    val dfs = UploadDateParserFactory.create(locale)
    var parseException: Exception? = null
    var date: LocalDate? = null
    for (df in dfs) {
        try {
            date = df.parse(uploadDate)
        } catch (e: Exception) {
            parseException = e
        }

        if (date != null) {
            cache.parser = df
            break
        }
    }

    return if (date == null) {
        AppLog.e("Cannot parse '$uploadDate' for '$locale'", Throwable("Cannot parse '$locale'", parseException))
        null
    } else {
        date
    }
}