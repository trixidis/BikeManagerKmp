package com.bikemanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Premium Dark Automotive color scheme
 * Based on PRD_UI_MES_ENTRETIENS_MOTO.md specifications
 */
private val DarkColorScheme = darkColorScheme(
    // Primary - Orange accent
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = AccentOrangeDark,
    onPrimaryContainer = Color.White,

    // Secondary - Teal accent
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentTealDark,
    onSecondaryContainer = Color.White,

    // Tertiary - Blue accent
    tertiary = AccentBlue,
    onTertiary = Color.White,

    // Background
    background = BgPrimary,
    onBackground = TextPrimary,

    // Surface
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgCardHover,
    onSurfaceVariant = TextSecondary,

    // Error
    error = ErrorRed,
    onError = Color.White,

    // Outline
    outline = BorderSubtle,
    outlineVariant = BorderStrong
)

/**
 * Premium Light Automotive color scheme
 * Complementary light theme with same accent colors
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Orange accent
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = AccentOrangeDark,
    onPrimaryContainer = Color.White,

    // Secondary - Teal accent
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentTealDark,
    onSecondaryContainer = Color.White,

    // Tertiary - Blue accent
    tertiary = AccentBlue,
    onTertiary = Color.White,

    // Background
    background = BgPrimaryLight,
    onBackground = TextPrimaryLight,

    // Surface
    surface = BgCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BgCardHoverLight,
    onSurfaceVariant = TextSecondaryLight,

    // Error
    error = ErrorRed,
    onError = Color.White,

    // Outline
    outline = BorderSubtleLight,
    outlineVariant = BorderStrongLight
)

/**
 * BikeManager theme with premium automotive design.
 *
 * Features:
 * - Automatic light/dark theme switching based on system settings
 * - Premium light theme (soft backgrounds, high contrast text)
 * - Premium dark theme (dark backgrounds #0A0A0B)
 * - Orange/Teal/Blue accent colors
 * - Custom typography (Archivo Black + DM Sans)
 * - Consistent spacing and dimensions
 *
 * @param darkTheme Optional override for theme mode. When null (default), follows system theme.
 *                  Set to true for dark theme, false for light theme.
 * @param content The composable content to theme
 */
@Composable
fun BikeManagerTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = darkTheme ?: systemInDarkTheme

    val colorScheme = if (useDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = createTypography(),
        content = content
    )
}
