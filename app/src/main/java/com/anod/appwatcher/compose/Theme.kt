package com.anod.appwatcher.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.preferences.Preferences

private val Rubik = FontFamily.Default

val typography = Typography()
val AppTypography = Typography(
    h4 = typography.h4.merge(TextStyle(fontFamily = Rubik)),
    h5 = typography.h5.merge(TextStyle(fontFamily = Rubik)),
    h6 = typography.h6.merge(TextStyle(fontFamily = Rubik)),
    subtitle1 = typography.subtitle1.merge(TextStyle(fontFamily = Rubik)),
    subtitle2 = typography.subtitle2.merge(TextStyle(fontFamily = Rubik)),
    body1 = typography.body1.merge(TextStyle(fontFamily = Rubik)),
    body2 = typography.body2.merge(TextStyle(fontFamily = Rubik)),
    button = typography.button.merge(TextStyle(fontFamily = Rubik)),
    caption = typography.caption.merge(TextStyle(fontFamily = Rubik)),
    overline = typography.overline.merge(TextStyle(fontFamily = Rubik))
)

private val Gray200 = Color(0xFFeeeeee)
private val BlueGray500 = Color(0xFF2196F3)
private val BlueGray800 = Color(0xFF1565C0)
private val BlueGray900 = Color(0xFF0D47A1)
private val Error = Color(0x7EC62828)
val WarningColor = Color(0xfff4511e)

private val DarkSurface = Color(0xFF263238)
private val DarkSurfaceVariant = Color(0xFF102027)

private val LightThemeColors = lightColors(
    primary = Color.White,
    primaryVariant = Gray200,
    onPrimary = Color.Black,
    secondary = BlueGray800,
    secondaryVariant = BlueGray900,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    error = Error,
    onError = Color.White,
)

private val DarkThemeColors = darkColors(
    primary = DarkSurface,
    primaryVariant = DarkSurface,
    onPrimary = Color.White,
    secondary = BlueGray500,
    secondaryVariant = BlueGray800,
    onSecondary = Color.White,
    background = DarkSurface,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White.copy(alpha = 0.8f),
    error = Error,
    onError = Color.White
)

private val BlackThemeColors = darkColors(
    primary = Color.Black,
    primaryVariant = Color.Black,
    onPrimary = Color.White,
    secondary = BlueGray800,
    secondaryVariant = BlueGray900,
    onSecondary = Color.White,
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

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: Int = Preferences.THEME_DEFAULT,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        if (theme == Preferences.THEME_BLACK) BlackThemeColors else DarkThemeColors
    } else LightThemeColors
    MaterialTheme(
        colors = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}