package com.bikemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import bikemanager.shared.generated.resources.*
import com.bikemanager.ui.Constants
import org.jetbrains.compose.resources.stringResource
import com.bikemanager.ui.theme.*
import com.bikemanager.ui.utils.formatNumber
import com.bikemanager.ui.utils.formatNumberDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Premium maintenance card component with Done/Todo variants.
 *
 * Based on PRD specifications:
 * - Icon wrapper 44×44dp with build icon
 * - Done variant: blue background, shows value + date
 * - Todo variant: teal background, shows hint + check indicator
 * - Border radius: 16dp
 * - Click on todo: mark as done
 * - Swipe to delete with confirmation snackbar
 *
 * @param maintenance The maintenance data
 * @param countingMethod Counting method (KM or HOURS)
 * @param onMarkDoneClick Callback when todo item is clicked
 * @param onDeleteSwipe Callback when item is swiped to delete
 * @param modifier Modifier to be applied
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceCard(
    maintenance: Maintenance,
    countingMethod: CountingMethod,
    onMarkDoneClick: () -> Unit,
    onDeleteSwipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = maintenance.value != null && maintenance.value >= 0
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDeleteSwipe()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(vertical = Dimens.SpaceSm),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.Space2xl)
                    .background(ErrorRed, RoundedCornerShape(Dimens.RadiusLg)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Space2xl)
                    .then(
                        if (!isDone) {
                            Modifier.clickable { onMarkDoneClick() }
                        } else Modifier
                    ),
                shape = RoundedCornerShape(Dimens.RadiusLg),
                colors = CardDefaults.cardColors(
                    containerColor = BgCard
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon wrapper
            IconWrapper(
                size = Dimens.MaintenanceCardIconSize,
                cornerRadius = Dimens.RadiusMd,
                background = if (isDone) {
                    Brush.linearGradient(
                        listOf(
                            AccentBlue.copy(alpha = 0.15f),
                            AccentBlue.copy(alpha = 0.15f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            AccentTeal.copy(alpha = 0.15f),
                            AccentTeal.copy(alpha = 0.15f)
                        )
                    )
                },
                shadowColor = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = if (isDone) AccentBlue else AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = maintenance.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                if (isDone) {
                    // Metadata for done maintenance
                    // Note: Using orange/gray dots temporarily instead of custom icons
                    // TODO: Create and use custom speedometer/clock/calendar SVG icons per PRD section 4.2
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Value with colored dot
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AccentOrange, CircleShape)
                            )
                            Text(
                                text = formatMaintenanceValue(
                                    maintenance.value!!,
                                    countingMethod
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AccentOrange
                            )
                        }

                        // Date
                        if (maintenance.date > 0) {
                            Text(
                                text = Constants.BULLET,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = formatDate(maintenance.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    // Hint for todo maintenance
                    Text(
                        text = stringResource(Res.string.press_to_mark_done),
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentTeal
                    )
                }
            }

            // Check indicator for todo
            if (!isDone) {
                Box(
                    modifier = Modifier
                        .size(Dimens.MaintenanceCheckSize)
                        .background(
                            AccentTeal.copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
            }
        }
    )
}

/**
 * Format maintenance value with unit
 */
private fun formatMaintenanceValue(value: Float, method: CountingMethod): String {
    val unit = if (method == CountingMethod.KM) "km" else "h"
    return if (value % 1.0f == 0.0f) {
        "${formatNumber(value.toInt())} $unit"
    } else {
        "${formatNumberDecimal(value)} $unit"
    }
}

/**
 * Format timestamp to dd/MM/yyyy
 */
private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${
        date.monthNumber.toString().padStart(2, '0')
    }/${date.year}"
}
