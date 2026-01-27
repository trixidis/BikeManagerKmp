package com.bikemanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.*

/**
 * Button variant types
 */
enum class ButtonVariant {
    /** Orange gradient - Primary actions */
    ORANGE,
    /** Teal gradient - Secondary/Todo actions */
    TEAL
}

/**
 * Premium primary button with gradient background and press animation.
 *
 * Based on PRD specifications:
 * - Height: 56dp
 * - Full width
 * - Border radius: 14dp
 * - Gradient backgrounds
 * - Shadow glow effect
 * - Press animation: scale 0.98
 * - Disabled state: opacity 0.5, no shadow
 *
 * @param text Button label text
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to be applied
 * @param variant Button color variant (Orange or Teal)
 * @param enabled Whether the button is enabled
 */
@Composable
fun ButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.ORANGE,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100)
    )

    val (background, shadowColor) = when (variant) {
        ButtonVariant.ORANGE -> Gradients.orange to Color(0xFFFF6B35)
        ButtonVariant.TEAL -> Gradients.teal to Color(0xFF00D9A5)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ButtonHeight)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = shadowColor.copy(alpha = if (enabled) 0.25f else 0f),
                spotColor = shadowColor.copy(alpha = if (enabled) 0.25f else 0f)
            )
            .background(
                brush = if (enabled) background else Brush.linearGradient(
                    listOf(BgCard, BgCard)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) Color.White else TextMuted
        )
    }
}
