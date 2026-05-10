package com.weathersnap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = LimeAccent,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = DarkText,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    secondary = CyanAccent,
    tertiary = AmberAccent
)

@Composable
fun WeatherSnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = WeatherSnapTypography,
        content = content
    )
}
