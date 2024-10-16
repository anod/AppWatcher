/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anod.appwatcher.utils.color

import androidx.annotation.ColorInt
import androidx.annotation.IntRange

/**
 * A utility class for common color variants used in Material themes.
 */
object MaterialColors {
    // Tone means degrees of lightness, in the range of 0 (inclusive) to 100 (inclusive).
    // Spec: https://m3.material.io/styles/color/the-color-system/color-roles
    private const val TONE_ACCENT_LIGHT = 40
    private const val TONE_ON_ACCENT_LIGHT = 100
    private const val TONE_ACCENT_CONTAINER_LIGHT = 90
    private const val TONE_ON_ACCENT_CONTAINER_LIGHT = 10
    private const val TONE_ACCENT_DARK = 80
    private const val TONE_ON_ACCENT_DARK = 20
    private const val TONE_ACCENT_CONTAINER_DARK = 30
    private const val TONE_ON_ACCENT_CONTAINER_DARK = 90

    /**
     * Returns the [ColorRoles] object generated from the provided input color.
     *
     * @param color The input color provided for generating its associated four color roles.
     * @param isLightTheme Whether the input is light themed or not, true if light theme is enabled.
     */
    fun getColorRoles(@ColorInt color: Int, isLightTheme: Boolean): ColorRoles {
        return if (isLightTheme) ColorRoles(
            getColorRole(color, TONE_ACCENT_LIGHT),
            getColorRole(color, TONE_ON_ACCENT_LIGHT),
            getColorRole(color, TONE_ACCENT_CONTAINER_LIGHT),
            getColorRole(color, TONE_ON_ACCENT_CONTAINER_LIGHT)
        ) else ColorRoles(
            getColorRole(color, TONE_ACCENT_DARK),
            getColorRole(color, TONE_ON_ACCENT_DARK),
            getColorRole(color, TONE_ACCENT_CONTAINER_DARK),
            getColorRole(color, TONE_ON_ACCENT_CONTAINER_DARK)
        )
    }

    @ColorInt
    private fun getColorRole(@ColorInt color: Int, @IntRange(from = 0, to = 100) tone: Int): Int {
        val hctColor = Hct.fromInt(color)
        hctColor.tone = tone.toDouble()
        return hctColor.toInt()
    }
}