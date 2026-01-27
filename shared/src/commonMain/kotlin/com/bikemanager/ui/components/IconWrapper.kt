package com.bikemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.Gradients

/**
 * Premium icon wrapper with gradient background and glow effect.
 * Used to display icons in a stylized container with shadow.
 *
 * Based on PRD specifications:
 * - Default size: 48dp × 48dp
 * - Default corner radius: 14dp
 * - Shadow with ambient and spot colors
 * - Gradient backgrounds
 *
 * @param modifier Modifier to be applied to the container
 * @param size Size of the container (default: 48dp)
 * @param cornerRadius Corner radius of the container (default: 14dp)
 * @param background Gradient brush for the background (default: orange gradient)
 * @param shadowColor Color of the shadow glow effect (default: orange)
 * @param content Icon or content to display inside the wrapper
 */
@Composable
fun IconWrapper(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 14.dp,
    background: Brush = Gradients.orange,
    shadowColor: Color = Color(0xFFFF6B35),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = shadowColor.copy(alpha = 0.25f),
                spotColor = shadowColor.copy(alpha = 0.25f)
            )
            .background(background, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
