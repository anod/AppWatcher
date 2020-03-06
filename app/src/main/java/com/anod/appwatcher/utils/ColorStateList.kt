// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun colorStateListOf(context: Context, vararg mapping: Pair<Int, Int>): ColorStateList {
    val (states, colors) = mapping.unzip()
    return ColorStateList(states.map { intArrayOf(it) }.toTypedArray(), colors.map { ContextCompat.getColor(context, it) }.toIntArray())
}

fun colorStateListOf(context: Context, @ColorRes id: Int): ColorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, id))
