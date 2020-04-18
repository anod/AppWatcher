// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

fun String?.lettersOrDigits(): String {
    return this?.filter { it.isLetterOrDigit() } ?: ""
}

fun String.compareLettersAndDigits(other: String?): Boolean {
    return this.lettersOrDigits() == other.lettersOrDigits()
}
