package com.bikemanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.*

/**
 * Snackbar type for styling
 */
enum class SnackbarType {
    /** Success snackbar with teal gradient */
    SUCCESS,
    /** Error snackbar with red background */
    ERROR,
    /** Info snackbar with blue background */
    INFO
}

/**
 * Premium snackbar with gradient backgrounds and icons.
 *
 * Based on PRD specifications:
 * - Gradient backgrounds for success/error states
 * - Icon + message layout
 * - Border radius: 14dp
 * - Shadow glow effect
 * - Elevated above content
 *
 * @param message Message to display
 * @param type Snackbar type (Success, Error, Info)
 * @param modifier Modifier to be applied
 */
@Composable
fun PremiumSnackbarContent(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    modifier: Modifier = Modifier
) {
    val (background, icon, iconTint) = when (type) {
        SnackbarType.SUCCESS -> Triple(
            Gradients.teal,
            Icons.Default.CheckCircle,
            Color.White
        )
        SnackbarType.ERROR -> Triple(
            Brush.linearGradient(listOf(ErrorRed, ErrorRed)),
            Icons.Default.Close,
            Color.White
        )
        SnackbarType.INFO -> Triple(
            Brush.linearGradient(listOf(AccentBlue, AccentBlue)),
            Icons.Default.Info,
            Color.White
        )
    }

    Box(
        modifier = modifier
            .padding(horizontal = Dimens.Space2xl, vertical = Dimens.SpaceBase)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = when (type) {
                    SnackbarType.SUCCESS -> AccentTeal.copy(alpha = 0.3f)
                    SnackbarType.ERROR -> ErrorRed.copy(alpha = 0.3f)
                    SnackbarType.INFO -> AccentBlue.copy(alpha = 0.3f)
                }
            )
            .background(background, RoundedCornerShape(14.dp))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Custom SnackbarHost that uses PremiumSnackbarContent.
 *
 * Usage:
 * ```
 * Scaffold(
 *     snackbarHost = {
 *         PremiumSnackbarHost(
 *             hostState = snackbarHostState,
 *             getSnackbarType = { message ->
 *                 when {
 *                     message.contains("succès", ignoreCase = true) -> SnackbarType.SUCCESS
 *                     message.contains("erreur", ignoreCase = true) -> SnackbarType.ERROR
 *                     else -> SnackbarType.INFO
 *                 }
 *             }
 *         )
 *     }
 * )
 * ```
 */
@Composable
fun PremiumSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    getSnackbarType: (String) -> SnackbarType = { SnackbarType.INFO }
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            val type = getSnackbarType(data.visuals.message)
            PremiumSnackbarContent(
                message = data.visuals.message,
                type = type
            )
        }
    )
}
