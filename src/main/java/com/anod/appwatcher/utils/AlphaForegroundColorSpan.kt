package com.anod.appwatcher.utils

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan

class AlphaSpannableString(val source: CharSequence, private val span: AlphaForegroundColorSpan) : SpannableString(source) {

    init {
        setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    var alpha: Float
        get() = span.alpha
        set(value) {
            span.alpha = value
            setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
}

class AlphaForegroundColorSpan(color: Int, var alpha: Float = 0f) : ForegroundColorSpan(color) {

    private val alphaColor: Int
        get() {
            val foregroundColor = foregroundColor
            return Color.argb((alpha * 255).toInt(), Color.red(foregroundColor), Color.green(foregroundColor), Color.blue(foregroundColor))
        }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = alphaColor
    }
}
