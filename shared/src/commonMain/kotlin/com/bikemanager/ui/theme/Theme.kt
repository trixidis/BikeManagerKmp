package com.bikemanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = White,
    secondary = Accent,
    onSecondary = White,
    background = White,
    onBackground = PrimaryText,
    surface = White,
    onSurface = PrimaryText,
    error = DarkRed,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = White,
    primaryContainer = DarkPrimaryVariant,
    onPrimaryContainer = White,
    secondary = DarkSecondary,
    onSecondary = White,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    error = DarkRed,
    onError = White
)

@Composable
fun BikeManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
