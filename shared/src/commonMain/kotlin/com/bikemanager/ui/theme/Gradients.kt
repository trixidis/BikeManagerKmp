package com.bikemanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Premium gradients for the "Premium Dark Automotive" design system.
 * Used for FAB, buttons, headers, and other accent elements.
 */
object Gradients {
    /**
     * Orange gradient for primary actions (FAB, primary buttons)
     */
    val orange = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF8F5E)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Teal gradient for secondary actions (todo items, secondary buttons)
     */
    val teal = Brush.linearGradient(
        colors = listOf(Color(0xFF00D9A5), Color(0xFF00F5BD)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Blue gradient for "Done" items and badges
     */
    val blue = Brush.linearGradient(
        colors = listOf(Color(0xFF4F7DF3), Color(0xFF7B9FF7)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Header gradient for "Faits" (Done) tab - Blue indigo theme (Dark)
     */
    val headerDoneDark = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A237E),
            Color(0xFF3F51B5),
            Color(0xFF5C6BC0)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Header gradient for "Faits" (Done) tab - Blue indigo theme (Light)
     */
    val headerDoneLight = Brush.linearGradient(
        colors = listOf(
            HeaderDoneStartLight,
            HeaderDoneMidLight,
            HeaderDoneEndLight
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Header gradient for "À faire" (Todo) tab - Teal/green theme (Dark)
     */
    val headerTodoDark = Brush.linearGradient(
        colors = listOf(
            Color(0xFF004D40),
            Color(0xFF009688),
            Color(0xFF26A69A)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Header gradient for "À faire" (Todo) tab - Teal/green theme (Light)
     */
    val headerTodoLight = Brush.linearGradient(
        colors = listOf(
            HeaderTodoStartLight,
            HeaderTodoMidLight,
            HeaderTodoEndLight
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Get the appropriate "Done" header gradient based on current theme.
     * Automatically detects theme from MaterialTheme.colorScheme.
     */
    @Composable
    fun getHeaderDone(): Brush {
        // Detect dark theme by checking if background is dark
        val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
        return if (isDark) headerDoneDark else headerDoneLight
    }

    /**
     * Get the appropriate "Todo" header gradient based on current theme.
     * Automatically detects theme from MaterialTheme.colorScheme.
     */
    @Composable
    fun getHeaderTodo(): Brush {
        // Detect dark theme by checking if background is dark
        val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
        return if (isDark) headerTodoDark else headerTodoLight
    }
}
