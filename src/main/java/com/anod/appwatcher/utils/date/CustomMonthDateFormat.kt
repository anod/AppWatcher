package com.anod.appwatcher.utils.date

import java.text.DateFormat
import java.text.FieldPosition
import java.text.ParsePosition
import java.util.*

/**
 * @author Alex Gavrishev
 * *
 * @date 12/09/2016.
 */

internal class CustomMonthDateFormat(private val monthNames: Array<String>) : DateFormat() {

    override fun format(date: Date, toAppendTo: StringBuffer, fieldPosition: FieldPosition): StringBuffer {
        val sb = StringBuffer()
        sb.append("d MMM. y г.")
        return sb
    }

    override fun parse(source: String, pos: ParsePosition): Date? {
        val start = pos.index
        val textLength = source.length

        var state = STATE_DAY
        var sb = StringBuilder()
        var day = 0
        var month = -1
        var year = 0
        // "d MMM. y г."
        for (index in start until textLength) {
            val ch = source[index]
            if (state == STATE_DAY) {
                if (ch == ' ') {
                    day = toInt(sb.toString())
                    if (day == -1) {
                        pos.index = 0
                        pos.errorIndex = index
                        break
                    }
                    sb = StringBuilder()
                    state = STATE_MONTH
                    continue
                }
            } else if (state == STATE_MONTH) {
                if (ch == '.') {
                    continue
                }
                if (ch == ' ') {
                    val monthName = sb.toString()
                    month = monthNames.indexOf(monthName)
                    if (month == -1) {
                        pos.index = 0
                        pos.errorIndex = index
                        break
                    }
                    sb = StringBuilder()
                    state = STATE_YEAR
                    continue
                }
            } else {// if (state == STATE_YEAR)
                if (ch == ' ') {
                    year = toInt(sb.toString())
                    if (year == -1) {
                        pos.index = 0
                        pos.errorIndex = index
                        break
                    }
                    break
                }
            }
            pos.index = index
            sb.append(ch)
        }

        if (day > 0 && month >= 0) {
            if (year == 0 && sb.length == 4) {
                year = toInt(sb.toString())
            }
            if (year > 2000) {
                val c = Calendar.getInstance()
                c.set(year, month, day)
                return c.time
            }
        }

        pos.index = 0
        return null
    }

    companion object {

        private const val STATE_DAY = 0
        private const val STATE_MONTH = 1
        private const val STATE_YEAR = 2

        private fun toInt(text: String): Int {
            if ("" == text) {
                return -1
            }
            try {
                return Integer.valueOf(text)!!
            } catch (ignored: NumberFormatException) {
            }

            return -1
        }
    }
}
