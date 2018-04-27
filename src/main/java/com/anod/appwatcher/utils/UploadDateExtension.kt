package com.anod.appwatcher.utils

import com.anod.appwatcher.utils.date.CustomParserFactory
import finsky.api.model.Document
import info.anodsplace.framework.AppLog
import java.text.DateFormat
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
    val dfs = CustomParserFactory.create(locale)
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

    if (dfs.isNotEmpty()) {
        if (date == null) {
            AppLog.e("Cannot parse '$uploadDate' for '$locale' using custom parser", Throwable("Cannot parse '$locale' using custom parser", parseException) )
        } else {
            return date
        }
    }

    val df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    try {
        return df.parse(uploadDate)
    } catch (e: ParseException) {
        AppLog.e("Cannot parse '$uploadDate' for '$locale' using system parser", Throwable("Cannot parse '$locale' using system parser", parseException) )
    }
    return null
}


