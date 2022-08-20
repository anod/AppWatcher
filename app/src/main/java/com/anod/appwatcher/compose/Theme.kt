package com.anod.appwatcher.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.anod.appwatcher.preferences.Preferences
import com.google.android.material.color.MaterialColors

private val AppTypography = Typography()

private val Gray200 = Color(0xFFeeeeee)
private val BlueGray500 = Color(0xFF2196F3)
private val BlueGray800 = Color(0xFF1565C0)
private val BlueGray900 = Color(0xFF0D47A1)
val Amber800 = Color(0xFFFF8F00)

private val LightThemeColors = lightColorScheme(
        primary = Color(0xFF2196F3),
        onPrimary = Color(0xFFffffff),
        primaryContainer = Color(0xFFbde9ff),
        onPrimaryContainer = Color(0xFF001f2a),
        secondary = Color(0xFF005db7),
        onSecondary = Color(0xFFffffff),
        secondaryContainer = Color(0xFFd6e3ff),
        onSecondaryContainer = Color(0xFF001b3d),
        tertiary = Color(0xFF2b5bb5),
        onTertiary = Color(0xFFffffff),
        tertiaryContainer = Color(0xFFd9e2ff),
        onTertiaryContainer = Color(0xFF001945),
        error = Color(0xFFba1a1a),
        onError = Color(0xFFffffff),
        errorContainer = Color(0xFFffdad6),
        onErrorContainer = Color(0xFF410002),

        background = Color(0xFFfafcff),
        onBackground = Color(0xFF001f2a),

        surface = Color(0xFFfafcff),
        onSurface = Color(0xFF001f2a),

        surfaceVariant = Color(0xFFdce4e9),
        onSurfaceVariant = Color(0xFF40484c),
        outline = Color(0xFF70787d)
)

private val DarkSurface = Color(0xFF263238)
private val DarkThemeColors = darkColorScheme(
        primary = Color(0xFF67d3ff),
        onPrimary = Color(0xFF003546),
        primaryContainer = Color(0xFF004d64),
        onPrimaryContainer = Color(0xFFbde9ff),
        secondary = Color(0xFF8ccdff),
        onSecondary = Color(0xFF00344e),
        secondaryContainer = Color(0xFF004b6f),
        onSecondaryContainer = Color(0xFFcae6ff),
        tertiary = Color(0xFF8ecdff),
        onTertiary = Color(0xFFffffff),
        tertiaryContainer = Color(0xFFcae6ff),
        onTertiaryContainer = Color(0xFF001e30),
        error = Color(0xFFffb4ab),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000a),
        onErrorContainer = Color(0xFFffdad6),

        background = DarkSurface,
        onBackground = Color(0xFFbde9ff),

        surface = DarkSurface,
        onSurface = Color(0xFFffffff),

        surfaceVariant = Color(0xFF40484c),
        onSurfaceVariant = Color(0xFFc0c8cd),
        outline = Color(0xFF8a9297)
)

private val BlackThemeColors = darkColorScheme(
        primary = Color(0xFF67d3ff),
        onPrimary = Color(0xFF003546),
        primaryContainer = Color(0xFF004d64),
        onPrimaryContainer = Color(0xFFbde9ff),
        secondary = Color(0xFF8ccdff),
        onSecondary = Color(0xFF00344e),
        secondaryContainer = Color(0xFF004b6f),
        onSecondaryContainer = Color(0xFFcae6ff),
        tertiary = Color(0xFF8ecdff),
        onTertiary = Color(0xFFffffff),
        tertiaryContainer = Color(0xFFcae6ff),
        onTertiaryContainer = Color(0xFF001e30),
        error = Color(0xFFffb4ab),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000a),
        onErrorContainer = Color(0xFFffdad6),

        background = Color.Black,
        onBackground = Color(0xFFbde9ff),

        surface = Color.Black,
        onSurface = Color(0xFFffffff),

        surfaceVariant = Color(0xFF40484c),
        onSurfaceVariant = Color(0xFFc0c8cd),
        outline = Color(0xFF8a9297)
)

val AppShapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
)

fun supportsDynamic(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun darkTheme(theme: Int, supportsDynamic: Boolean): ColorScheme {
    return if (theme == Preferences.THEME_BLACK) {
        if (supportsDynamic) {
            dynamicDarkColorScheme(LocalContext.current).copy(
                    background = Color.Black,
                    surface = Color.Black,
            )
        } else BlackThemeColors
    } else {
        if (supportsDynamic) dynamicDarkColorScheme(LocalContext.current) else DarkThemeColors
    }
}

@Composable
fun AppTheme(
        theme: Int = Preferences.THEME_DEFAULT,
        darkTheme: Boolean = theme == Preferences.THEME_BLACK || isSystemInDarkTheme(),
        customPrimaryColor: Color? = null,
        content: @Composable () -> Unit
) {

    var colorScheme = if (supportsDynamic()) {
        if (darkTheme) darkTheme(theme, supportsDynamic = true) else dynamicLightColorScheme(LocalContext.current)
    } else {
        if (darkTheme) darkTheme(theme, supportsDynamic = false) else LightThemeColors
    }

    var statusBarColor = colorScheme.surface.toArgb()
    var isAppearanceLightStatusBars = !darkTheme
    if (customPrimaryColor != null) {
        val roles = MaterialColors.getColorRoles(customPrimaryColor.toArgb(), !darkTheme)
        colorScheme = colorScheme.copy(
                primary = Color(roles.accent), //customPrimaryColor,
                onPrimary = Color(roles.onAccent),
                primaryContainer = Color(roles.accentContainer),
                onPrimaryContainer = Color(roles.onAccentContainer)
        )
        statusBarColor = colorScheme.primary.toArgb()
        isAppearanceLightStatusBars = MaterialColors.isColorLight(statusBarColor)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = statusBarColor
            (view.context as Activity).window.navigationBarColor = colorScheme.surface.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = isAppearanceLightStatusBars
        }
    }

    MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
    )
}