package com.bikemanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
 * BikeManager theme with premium dark automotive design.
 *
 * Features:
 * - Dark backgrounds (#0A0A0B)
 * - Orange/Teal/Blue accent colors
 * - Custom typography (Archivo Black + DM Sans)
 * - Consistent spacing and dimensions
 */
@Composable
fun BikeManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = createTypography(),
        content = content
    )
}
