package com.anod.appwatcher.utils

import com.anod.appwatcher.utils.date.UploadDateParserFactory
import finsky.api.model.Document
import info.anodsplace.applog.AppLog
import java.text.ParseException
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
    if (uploadDate.isBlank()) {
        return null
    }

    var date: Date? = null
    val dfs = UploadDateParserFactory.create(locale)
    var parseException: ParseException? = null
    for (df in dfs) {
        try {
            date = df.parse(uploadDate)
        } catch (e: ParseException) {
            parseException = e
        }

        if (date != null) {
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


