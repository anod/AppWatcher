package com.anod.appwatcher.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.preferences.Preferences

private val Rubik = FontFamily.Default

val typography = Typography()
val AppTypography = Typography(
        displayLarge = typography.displayLarge.merge(TextStyle(fontFamily = Rubik)),
        displayMedium = typography.displayMedium.merge(TextStyle(fontFamily = Rubik)),
        displaySmall = typography.displaySmall.merge(TextStyle(fontFamily = Rubik)),
        headlineLarge = typography.headlineLarge.merge(TextStyle(fontFamily = Rubik)),
        headlineMedium = typography.headlineMedium.merge(TextStyle(fontFamily = Rubik)),
        headlineSmall = typography.headlineSmall.merge(TextStyle(fontFamily = Rubik)),
        titleLarge = typography.titleLarge.merge(TextStyle(fontFamily = Rubik)),
        titleMedium = typography.titleMedium.merge(TextStyle(fontFamily = Rubik)),
        titleSmall = typography.titleSmall.merge(TextStyle(fontFamily = Rubik)),
        bodyLarge = typography.bodyLarge.merge(TextStyle(fontFamily = Rubik)),
        bodyMedium = typography.bodyMedium.merge(TextStyle(fontFamily = Rubik)),
        bodySmall = typography.bodySmall.merge(TextStyle(fontFamily = Rubik)),
        labelLarge = typography.labelLarge.merge(TextStyle(fontFamily = Rubik)),
        labelMedium = typography.labelMedium.merge(TextStyle(fontFamily = Rubik)),
        labelSmall = typography.labelSmall.merge(TextStyle(fontFamily = Rubik)),
)

private val Gray200 = Color(0xFFeeeeee)
private val BlueGray500 = Color(0xFF2196F3)
private val BlueGray800 = Color(0xFF1565C0)
private val BlueGray800HighContrast = Color(0xFF5199EC)
private val BlueGray900 = Color(0xFF0D47A1)
private val BlueGray900HighContrast = Color(0xFF1D70ED)

private val Error = Color(0x7EC62828)
val WarningColor = Color(0xfff4511e)

private val DarkSurface = Color(0xFF263238)

private val LightThemeColors = lightColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        secondary = BlueGray800,
        onSecondary = Color.White,
        tertiary = BlueGray900,
        surface = Color.White,
        onSurface = Color.Black,
        background = Color.White,
        onBackground = Color.Black,
        error = Error,
        onError = Color.White,
)

private val DarkThemeColors = darkColorScheme(
        primary = DarkSurface,
        onPrimary = Color.White,
        secondary = BlueGray500,
        onSecondary = Color.White,
        tertiary = BlueGray800,
        background = DarkSurface,
        onBackground = Color.White,
        surface = DarkSurface,
        onSurface = Color.White.copy(alpha = 0.8f),
        error = Error,
        onError = Color.White
)

private val BlackThemeColors = darkColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        secondary = BlueGray800HighContrast,
        onSecondary = Color.White,
        tertiary = BlueGray900HighContrast,
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White.copy(alpha = 0.8f),
        error = Error,
        onError = Color.White
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
                    // TODO
            )
        } else BlackThemeColors
    } else {
        if (supportsDynamic) dynamicDarkColorScheme(LocalContext.current) else DarkThemeColors
    }
}

@Composable
fun AppTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        theme: Int = Preferences.THEME_DEFAULT,
        content: @Composable () -> Unit
) {

    val colors = if (supportsDynamic()) {
        if (darkTheme) darkTheme(theme, supportsDynamic = true) else dynamicLightColorScheme(LocalContext.current)
    } else {
        if (darkTheme) darkTheme(theme, supportsDynamic = false) else LightThemeColors
    }

    MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
    )
}