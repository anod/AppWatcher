package com.anod.appwatcher.utils.date

import java.text.DateFormat
import java.text.FieldPosition
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date

/**
 * Parse "d MMM. y" and "d-MMM-y"
 */
internal class CustomMonthDateFormat(
    private val monthNames: Array<String>,
    private val order: Array<STATE>,
    private val clean: String = ""
) : DateFormat() {

    override fun format(
        date: Date,
        toAppendTo: StringBuffer,
        fieldPosition: FieldPosition
    ): StringBuffer {
        val cal = Calendar.getInstance()
        cal.time = date
        val monthIdx = cal.get(Calendar.MONTH)
        return StringBuffer().append(order.joinToString(" ") { state ->
            when (state) {
                STATE.DAY -> cal.get(Calendar.DAY_OF_MONTH).toString()
                STATE.MONTH -> monthNames[monthIdx]
                STATE.YEAR -> cal.get(Calendar.YEAR).toString()
                STATE.UNKNOWN -> throw IllegalStateException("UNKNOWN is not valid")
            }
        })
    }

    override fun parse(date: String, pos: ParsePosition): Date? {
        assert(order.size == 3)

        val source = if (clean.isEmpty()) date else date.replace(clean, "")
        val start = pos.index
        val textLength = source.length

        var stateIndex = 0
        var state: STATE = STATE.UNKNOWN
        var sb = StringBuilder()
        var day = 0
        var month = -1
        var year = 0
        // "d MMM. y г.", "d-MMM-y", "MMM d, y"
        for (index in start until textLength) {
            val ch = source[index]
            if (state == STATE.UNKNOWN) {
                if (stopChar[ch] == true) {
                    continue
                }
                state = order[stateIndex]
            }
            if (state == STATE.DAY) {
                if (stopChar[ch] == true) {
                    day = toInt(sb.toString())
                    if (day == -1) {
                        pos.index = 0
                        pos.errorIndex = index
                        break
                    }
                    sb = StringBuilder()
                    stateIndex++
                    state = STATE.UNKNOWN
                    continue
                }
            } else if (state == STATE.MONTH) {
                if (stopChar[ch] == true) {
                    val monthName = sb.toString()
                    month = monthNames.indexOf(monthName)
                    if (month == -1) {
                        pos.index = 0
                        pos.errorIndex = index
                        break
                    }
                    sb = StringBuilder()
                    stateIndex++
                    state = STATE.UNKNOWN
                    continue
                }
            } else if (state == STATE.YEAR) {
                if (stopChar[ch] == true) {
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
        val ORDER_DMY = arrayOf(STATE.DAY, STATE.MONTH, STATE.YEAR)
        val ORDER_MDY = arrayOf(STATE.MONTH, STATE.DAY, STATE.YEAR)

        val stopChar = mapOf(
                ' ' to true,
                ',' to true,
                '.' to true,
                '-' to true
        )

        enum class STATE {
            UNKNOWN,
            DAY,
            MONTH,
            YEAR,
        }

        private fun toInt(text: String): Int {
            if ("" == text) {
                return -1
            }
            try {
                return Integer.valueOf(text)
            } catch (ignored: NumberFormatException) {
            }

            return -1
        }
    }
}