package com.bikemanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Premium typography for the "Premium Dark Automotive" design system.
 *
 * FONTS:
 * - Display/Title styles: Archivo Black (bold, condensed)
 * - Body/Label styles: DM Sans (clean, modern)
 *
 * Custom fonts configuration:
 * 1. Download fonts from Google Fonts (see androidApp/src/main/res/font/README.md)
 * 2. Place font files in androidApp/src/main/res/font/
 * 3. Platform-specific implementations:
 *    - Android: androidApp/src/main/kotlin/com/bikemanager/ui/theme/TypeActual.kt
 *    - iOS: To be implemented
 *
 * Fonts automatically fallback to system fonts if not found.
 */

/**
 * Archivo Black font family for titles.
 * Platform-specific implementation required.
 */
expect val ArchivoBlack: FontFamily

/**
 * DM Sans font family for body text.
 * Platform-specific implementation required.
 */
expect val DMSans: FontFamily

/**
 * Creates the typography system for the app.
 * Uses Archivo Black for titles and DM Sans for body text.
 *
 * Fonts are loaded via expect/actual pattern:
 * - Android: From androidApp/src/main/res/font/
 * - Fallback: System fonts if custom fonts not available
 */
@Composable
fun createTypography(): Typography {
    return Typography(
        // ========== Display Styles (Archivo Black) ==========
        displayLarge = TextStyle(
            fontFamily = ArchivoBlack,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            lineHeight = 30.8.sp,
            letterSpacing = (-0.5).sp,
            color = TextPrimary
        ),
        displayMedium = TextStyle(
            fontFamily = ArchivoBlack,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            lineHeight = 28.6.sp,
            letterSpacing = (-0.3).sp,
            color = TextPrimary
        ),

        // ========== Title Styles (Archivo Black) ==========
        titleLarge = TextStyle(
            fontFamily = ArchivoBlack,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.3).sp,
            color = TextPrimary
        ),
        titleMedium = TextStyle(
            fontFamily = ArchivoBlack,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            lineHeight = 21.6.sp,
            letterSpacing = (-0.3).sp,
            color = TextPrimary
        ),

        // ========== Body Styles (DM Sans) ==========
        bodyLarge = TextStyle(
            fontFamily = DMSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
            color = TextPrimary
        ),
        bodyMedium = TextStyle(
            fontFamily = DMSans,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 21.sp,
            letterSpacing = 0.sp,
            color = TextPrimary
        ),
        bodySmall = TextStyle(
            fontFamily = DMSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.6.sp,
            letterSpacing = 0.sp,
            color = TextSecondary
        ),

        // ========== Label Styles (DM Sans) ==========
        labelLarge = TextStyle(
            fontFamily = DMSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp,
            letterSpacing = 0.sp,
            color = TextPrimary
        ),
        labelMedium = TextStyle(
            fontFamily = DMSans,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 13.sp,
            letterSpacing = 0.5.sp,
            color = TextSecondary
        ),
        labelSmall = TextStyle(
            fontFamily = DMSans,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.9.sp,
            letterSpacing = 0.sp,
            color = TextSecondary
        )
    )
}
