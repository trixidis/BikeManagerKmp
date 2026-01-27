package com.bikemanager.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*

/**
 * Standardized animations for the "Premium Dark Automotive" design system.
 * Provides consistent timing, easing, and transition patterns across the app.
 */
object Animations {
    // ========== Durations ==========
    /** 150ms - Fast micro-interactions (press, hover start) */
    const val DURATION_FAST = 150

    /** 300ms - Normal transitions (color changes, smooth animations) */
    const val DURATION_NORMAL = 300

    /** 500ms - Slow entrance animations (list items, complex transitions) */
    const val DURATION_SLOW = 500

    // ========== Easings ==========
    /** Ease-out curve for smooth deceleration (exits, reveals) */
    val EaseOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** Ease-in-out curve for smooth acceleration and deceleration (color transitions) */
    val EaseInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    // ========== Enter Transitions ==========
    /**
     * Fade in + slide up - Used for list items, cards appearing
     * 500ms duration with ease-out easing
     */
    val fadeInUp = fadeIn(
        animationSpec = tween(DURATION_SLOW, easing = EaseOut)
    ) + slideInVertically(
        initialOffsetY = { 20 },
        animationSpec = tween(DURATION_SLOW, easing = EaseOut)
    )

    /**
     * Slide in from right - Used for screen transitions (list -> details)
     * 400ms duration with ease-out easing
     */
    val slideInRight = fadeIn(
        animationSpec = tween(400, easing = EaseOut)
    ) + slideInHorizontally(
        initialOffsetX = { 40 },
        animationSpec = tween(400, easing = EaseOut)
    )

    /**
     * Scale in - Used for modal/dialog appearances
     * 300ms duration with ease-out easing, starts at 90% scale
     */
    val scaleIn = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(DURATION_NORMAL, easing = EaseOut)
    )

    // ========== Stagger Delay ==========
    /**
     * Calculate stagger delay for list animations
     * Each item appears 50ms after the previous one, capped at 200ms
     *
     * @param index Position in the list (0-indexed)
     * @return Delay in milliseconds
     *
     * Examples:
     * - Item 0: 0ms
     * - Item 1: 50ms
     * - Item 2: 100ms
     * - Item 3: 150ms
     * - Item 4+: 200ms (capped)
     */
    fun staggerDelay(index: Int): Int = minOf(index * 50, 200)
}
