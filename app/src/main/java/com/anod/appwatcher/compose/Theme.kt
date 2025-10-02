package com.anod.appwatcher.compose

import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.color.MaterialColors
import com.anod.appwatcher.utils.isLightColor
import info.anodsplace.framework.app.CustomThemeColor
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.WindowCustomTheme
import info.anodsplace.framework.app.findActivity
import info.anodsplace.framework.app.findWindow

private val AppTypography = Typography()
val Amber800 = Color(0xFFFF8F00)

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun darkTheme(theme: Int): ColorScheme {
    return if (theme == Preferences.THEME_BLACK) {
        dynamicDarkColorScheme(LocalContext.current).copy(
            background = Color.Black,
            surface = Color.Black,
        )
    } else {
        dynamicDarkColorScheme(LocalContext.current)
    }
}

@Composable
fun AppTheme(
    theme: Int = Preferences.THEME_DEFAULT,
    darkTheme: Boolean = theme == Preferences.THEME_BLACK || isSystemInDarkTheme(),
    customPrimaryColor: Color? = null,
    updateSystemBars: Boolean = true,
    useSurfaceAsPrimary: Boolean = false,
    transparentSystemUi: Boolean = false,
    content: @Composable () -> Unit
) {
    var colorScheme = if (darkTheme) darkTheme(theme) else dynamicLightColorScheme(LocalContext.current)

    var statusBarColor = colorScheme.surface
    var isAppearanceLightStatusBars = !darkTheme
    if (customPrimaryColor != null) {
        val roles = MaterialColors.getColorRoles(customPrimaryColor.toArgb(), !darkTheme)
        colorScheme = colorScheme.copy(
            primary = Color(roles.accent), // customPrimaryColor,
            onPrimary = Color(roles.onAccent),
            primaryContainer = Color(roles.accentContainer),
            onPrimaryContainer = Color(roles.onAccentContainer)
        )
        statusBarColor = colorScheme.primary
        isAppearanceLightStatusBars = statusBarColor.isLightColor
    } else if (useSurfaceAsPrimary) {
        colorScheme = colorScheme.copy(
            primary = colorScheme.surface,
            // wait for customPrimaryColor to set OnPrimary
        )
    }

    if (updateSystemBars) {
        if (transparentSystemUi) {
            setSystemUiColors(
                statusBarColor = Color.Transparent,
                statusBarDarkIcons = !darkTheme,
                navigationBarColor = Color.Transparent,
                navigationBarDarkIcons = !darkTheme
            )
        } else {
            setSystemUiColors(
                statusBarColor = statusBarColor,
                statusBarDarkIcons = isAppearanceLightStatusBars,
                navigationBarColor = Color.Transparent,
                navigationBarDarkIcons = !darkTheme
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun setSystemUiColors(
    statusBarColor: Color,
    statusBarDarkIcons: Boolean,
    navigationBarColor: Color,
    navigationBarDarkIcons: Boolean
): Boolean {
    val window = findWindow() ?: return false
    val activity = LocalContext.current.findActivity()
    WindowCustomTheme.apply(
        themeColors = CustomThemeColors(
            statusBarColor = CustomThemeColor(
                colorInt = statusBarColor.toArgb(),
                isLight = statusBarDarkIcons
            ),
            navigationBarColor = CustomThemeColor(
                colorInt = navigationBarColor.toArgb(),
                isLight = navigationBarDarkIcons
            )
        ),
        window = window,
        activity = activity
    )
    return true
}

@Composable
private fun findWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window
    ?: LocalView.current.context.findWindow()