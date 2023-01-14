package com.anod.appwatcher.utils

import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.date.UploadDateParserFactory
import finsky.api.Document
import info.anodsplace.applog.AppLog
import java.text.ParseException
import java.util.Date
import java.util.Locale

/**
 * @author alex
 * *
 * @date 2015-02-23
 */

fun Document.extractUploadDate(cache: UploadDateParserCache, locale: Locale = Locale.getDefault()): Long {
    val uploadDate = this.appDetails.uploadDate ?: return 0
    val date = extractUploadDate(uploadDate, locale, cache) ?: return 0
    return date.time
}

fun extractUploadDate(uploadDate: String, locale: Locale, cache: UploadDateParserCache): Date? {
    if (uploadDate.isBlank()) {
        return null
    }

    var date: Date? = null

    if (cache.parser != null) {
        try {
            date = cache.parser!!.parse(uploadDate)
            if (date != null) {
                return date
            }
        } catch (e: ParseException) {
            AppLog.e("Cannot parse '$uploadDate' for '$locale' with cached parser")
        }
    }

    val dfs = UploadDateParserFactory.create(locale)
    var parseException: ParseException? = null
    for (df in dfs) {
        try {
            date = df.parse(uploadDate)
        } catch (e: ParseException) {
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