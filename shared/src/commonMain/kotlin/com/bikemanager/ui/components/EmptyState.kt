package com.bikemanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.*

/**
 * Premium empty state component with floating animation.
 *
 * Based on PRD specifications:
 * - Icon container: rounded square with BgCard background
 * - Floating animation: 3 second cycle, -6dp vertical movement
 * - Icon + title + description layout
 * - Centered alignment
 *
 * @param icon Icon to display in the container
 * @param title Main title text
 * @param description Secondary description text
 * @param modifier Modifier to be applied
 * @param iconSize Size of the icon (default: 48dp)
 * @param containerSize Size of the icon container (default: 100dp)
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    containerSize: Dp = 100.dp
) {
    // Floating animation - 3 second cycle
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0 with EaseInOut
                -6f at 1500 with EaseInOut
                0f at 3000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Floating icon container
        Box(
            modifier = Modifier
                .size(containerSize)
                .graphicsLayer {
                    translationY = offsetY
                }
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 260.dp)
        )
    }
}

/**
 * Simplified empty state with just a message.
 * Uses default icon and no description.
 *
 * @param message Message to display
 * @param modifier Modifier to be applied
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Build,
        title = message,
        description = "",
        modifier = modifier
    )
}
