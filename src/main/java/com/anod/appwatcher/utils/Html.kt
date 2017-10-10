package com.anod.appwatcher.utils

import android.text.SpannableString
import android.text.Spanned
import info.anodsplace.android.log.AppLog


/**
 * @author algavris
 * @date 14/09/2017
 */
object Html {

    fun parse(source: String): Spanned {
        try {
            if (source.isBlank()) {
                return SpannableString(source)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return android.text.Html.fromHtml(source, android.text.Html.FROM_HTML_MODE_COMPACT)
            }
            return android.text.Html.fromHtml(source)
        } catch (e: RuntimeException) {
            AppLog.e(e)
            return SpannableString(source)
        }
    }
}