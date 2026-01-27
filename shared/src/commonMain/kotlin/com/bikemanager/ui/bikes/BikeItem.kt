package com.bikemanager.ui.bikes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Build
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bikemanager.shared.generated.resources.*
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.ui.Constants
import com.bikemanager.ui.components.IconWrapper
import com.bikemanager.ui.theme.*
import org.jetbrains.compose.resources.stringResource

/**
 * Premium bike card component with hover effects and animations.
 *
 * Based on PRD specifications:
 * - Icon wrapper 48×48dp with motorcycle icon
 * - Edit button 40×40dp
 * - Bike stat with icon (speedometer/clock) + formatted value
 * - Method badge (KILOMÈTRES/HEURES)
 * - Hover: translateY -2dp, border glow, top accent bar
 * - Press: scale 0.98
 * - Long press: opens delete confirmation
 * - Border radius: 20dp
 *
 * @param bike The bike to display
 * @param totalKmOrHours Total km or hours (calculated from maintenance records)
 * @param onClick Callback when card is tapped
 * @param onEditClick Callback when edit button is tapped
 * @param onLongPress Callback when card is long-pressed (for delete)
 * @param modifier Modifier to be applied
 */
@Composable
fun BikeItem(
    bike: Bike,
    totalKmOrHours: Float,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100)
    )

    val translateY by animateDpAsState(
        targetValue = if (isHovered) (-2).dp else 0.dp,
        animationSpec = tween(300, easing = Animations.EaseOut)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space2xl, vertical = Dimens.SpaceSm)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = translateY.toPx()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        shape = RoundedCornerShape(Dimens.RadiusXl),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) BgCardHover else BgCard
        ),
        border = BorderStroke(
            1.dp,
            if (isHovered) BorderStrong else BorderSubtle
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Top accent bar (visible on hover)
            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Gradients.orange)
                )
            }

            Column(
                modifier = Modifier.padding(Dimens.Space2xl)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconWrapper(
                        size = Dimens.BikeCardIconSize,
                        cornerRadius = Dimens.BikeCardIconRadius,
                        background = Gradients.orange,
                        shadowColor = AccentOrange
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Edit button only (no delete button per PRD)
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(Dimens.BikeCardEditBtnSize)
                            .background(
                                Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(Dimens.RadiusMd)
                            )
                            .border(
                                1.dp,
                                BorderSubtle,
                                RoundedCornerShape(Dimens.RadiusMd)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.edit),
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceLg))

                // Bike name
                Text(
                    text = bike.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceLg))

                // Stats row with value and method badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXl),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BikeStat(
                        value = totalKmOrHours,
                        countingMethod = bike.countingMethod
                    )
                    MethodBadge(method = bike.countingMethod)
                }
            }
        }
    }
}

/**
 * Bike stat showing total km/hours with colored dot
 * PRD specs:
 * - Icon: speedometer (km) or clock (hours), 16dp, accent-orange
 * - Value: body-sm (14sp), font-weight 600, text-primary
 * - Format: "12,500 km" or "156 h" (with thousand separators as spaces)
 *
 * Note: Using orange dot temporarily instead of custom icons
 * TODO: Create and use custom speedometer/clock SVG icons per PRD section 4.2
 */
@Composable
private fun BikeStat(value: Float, countingMethod: CountingMethod) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AccentOrange, CircleShape)
        )

        Text(
            text = formatBikeValue(value, countingMethod),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary
        )
    }
}

/**
 * Badge showing the counting method (KILOMÈTRES or HEURES)
 */
@Composable
private fun MethodBadge(method: CountingMethod) {
    Box(
        modifier = Modifier
            .background(
                AccentBlue.copy(alpha = 0.15f),
                RoundedCornerShape(Dimens.RadiusSm)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (method == CountingMethod.KM) stringResource(Res.string.kilometers_badge) else stringResource(Res.string.hours_badge),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            ),
            color = AccentBlue
        )
    }
}

/**
 * Format bike value with thousand separators (spaces) and unit
 */
private fun formatBikeValue(value: Float, method: CountingMethod): String {
    val unit = if (method == CountingMethod.KM) Constants.KM_UNIT else Constants.HOURS_UNIT
    return if (value % 1.0f == 0.0f) {
        "${String.format("%,d", value.toInt()).replace(',', ' ')} $unit"
    } else {
        "${String.format("%,.1f", value).replace(',', ' ')} $unit"
    }
}
