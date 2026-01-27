package com.bikemanager.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
     * Header gradient for "Faits" (Done) tab - Blue indigo theme
     */
    val headerDone = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A237E),
            Color(0xFF3F51B5),
            Color(0xFF5C6BC0)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /**
     * Header gradient for "À faire" (Todo) tab - Teal/green theme
     */
    val headerTodo = Brush.linearGradient(
        colors = listOf(
            Color(0xFF004D40),
            Color(0xFF009688),
            Color(0xFF26A69A)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
}
