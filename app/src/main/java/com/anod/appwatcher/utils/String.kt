// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

fun String?.lettersOrDigits(): String = this?.filter { it.isLetterOrDigit() } ?: ""

fun String.compareLettersAndDigits(other: String?): Boolean = this.lettersOrDigits() == other.lettersOrDigits()