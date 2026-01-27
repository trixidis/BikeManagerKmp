package com.bikemanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.*

/**
 * FAB variant types
 */
enum class FabVariant {
    /** Orange gradient - Primary/Done actions */
    ORANGE,
    /** Teal gradient - Todo actions */
    TEAL
}

/**
 * Premium floating action button with gradient and press animation.
 *
 * Based on PRD specifications:
 * - Size: 60dp × 60dp
 * - Border radius: 18dp
 * - Gradient backgrounds
 * - Shadow glow effect
 * - Press animation: scale 0.95
 * - Hover animation: scale 1.08
 *
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier to be applied
 * @param variant FAB color variant (Orange or Teal)
 * @param icon Icon to display (default: Add)
 */
@Composable
fun Fab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FabVariant = FabVariant.ORANGE,
    icon: ImageVector = Icons.Default.Add
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100)
    )

    val (background, shadowColor) = when (variant) {
        FabVariant.ORANGE -> Gradients.orange to AccentOrange
        FabVariant.TEAL -> Gradients.teal to AccentTeal
    }

    Box(
        modifier = modifier
            .size(Dimens.FabSize)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(Dimens.FabRadius),
                ambientColor = shadowColor.copy(alpha = 0.25f),
                spotColor = shadowColor.copy(alpha = 0.25f)
            )
            .background(background, RoundedCornerShape(Dimens.FabRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
