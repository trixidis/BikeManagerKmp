package com.bikemanager.ui.extensions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.Animations

/**
 * Extension functions for common animation patterns in the Premium Dark Automotive design.
 */

/**
 * Staggered animation for list items.
 * Each item appears with a delay based on its index.
 *
 * @param index Position in the list (0-indexed)
 * @param delayMillis Base delay per item (default: 50ms)
 * @param maxDelay Maximum delay cap (default: 200ms)
 * @return EnterTransition with calculated delay
 */
fun staggeredFadeInUp(
    index: Int,
    delayMillis: Int = 50,
    maxDelay: Int = 200
): EnterTransition {
    val delay = minOf(index * delayMillis, maxDelay)

    return fadeIn(
        animationSpec = tween(
            durationMillis = Animations.DURATION_SLOW,
            delayMillis = delay,
            easing = Animations.EaseOut
        )
    ) + slideInVertically(
        initialOffsetY = { 20 },
        animationSpec = tween(
            durationMillis = Animations.DURATION_SLOW,
            delayMillis = delay,
            easing = Animations.EaseOut
        )
    )
}

/**
 * Remember FAB visibility based on scroll state.
 * FAB hides when scrolling down, shows when scrolling up or at top.
 *
 * @param listState LazyListState to observe
 * @return Boolean indicating if FAB should be visible
 */
@Composable
fun rememberFabVisibility(listState: LazyListState): Boolean {
    val fabVisible by remember {
        derivedStateOf {
            // Show FAB when at the top or not scrolling
            listState.firstVisibleItemIndex == 0 || !listState.isScrollInProgress
        }
    }
    return fabVisible
}

/**
 * Animate color transition for header gradients.
 * Used when switching between tabs in maintenances screen.
 *
 * @param targetColor Target color to animate to
 * @param durationMillis Duration of animation (default: 300ms)
 * @return Animated color state
 */
@Composable
fun animateHeaderColor(
    targetColor: androidx.compose.ui.graphics.Color,
    durationMillis: Int = Animations.DURATION_NORMAL
): State<androidx.compose.ui.graphics.Color> {
    return animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = Animations.EaseInOut
        )
    )
}

/**
 * Modifier for hover animation (translateY -2dp).
 * Used on cards and interactive elements.
 *
 * @param isHovered Whether element is currently hovered
 * @return Modifier with translation animation
 */
@Composable
fun Modifier.hoverAnimation(isHovered: Boolean): Modifier {
    val translateY by animateDpAsState(
        targetValue = if (isHovered) (-2).dp else 0.dp,
        animationSpec = tween(
            durationMillis = Animations.DURATION_NORMAL,
            easing = Animations.EaseOut
        )
    )

    return this.then(
        Modifier.graphicsLayer {
            translationY = translateY.toPx()
        }
    )
}

/**
 * Modifier for press animation (scale 0.98).
 * Used on buttons and clickable cards.
 *
 * @param isPressed Whether element is currently pressed
 * @return Modifier with scale animation
 */
@Composable
fun Modifier.pressAnimation(isPressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Animations.DURATION_FAST)
    )

    return this.then(
        Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}
