package com.anod.appwatcher.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val Color.isLightColor: Boolean
    get() = this.luminance() > 0.5