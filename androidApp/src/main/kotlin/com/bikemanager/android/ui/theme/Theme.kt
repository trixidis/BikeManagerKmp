package com.bikemanager.android.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Premium Dark Automotive color scheme for Android
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
 * - Dark status bar for immersive experience
 * - Consistent spacing and dimensions
 */
@Composable
fun BikeManagerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to primary background color for seamless dark theme
            window.statusBarColor = BgPrimary.toArgb()
            // Dark content for dark status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
