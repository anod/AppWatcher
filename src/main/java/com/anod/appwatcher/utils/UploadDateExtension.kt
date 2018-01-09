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
    if (uploadDate.isBlank()) {
        return null
    }
    var date: Date? = null
    var df = CustomParserFactory.create(locale)
    if (df != null) {
        try {
            date = df.parse(uploadDate)
        } catch (e: ParseException) {
        }

        if (date != null) {
            return date
        }
    }

    df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    try {
        return df.parse(uploadDate)
    } catch (e: ParseException) {
    }
    return null
}


