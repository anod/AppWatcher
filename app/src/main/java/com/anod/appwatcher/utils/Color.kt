package com.anod.appwatcher.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val Color.isLightColor: Boolean
    get() = this.luminance() > 0.5

// Luminance where contrast against black overtakes contrast against white, per the WCAG ratio formula
private const val BLACK_OVER_WHITE_LUMINANCE = 0.179f

/** Black or white, whichever is more readable on top of this color. */
val Color.contentColor: Color
    get() = if (this.luminance() > BLACK_OVER_WHITE_LUMINANCE) Color.Black else Color.White