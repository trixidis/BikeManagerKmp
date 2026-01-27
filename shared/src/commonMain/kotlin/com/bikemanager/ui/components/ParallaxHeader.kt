package com.bikemanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikemanager.domain.model.CountingMethod
import bikemanager.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.bikemanager.ui.theme.*
import com.bikemanager.ui.utils.formatNumber
import com.bikemanager.ui.utils.formatNumberDecimal

/**
 * Premium parallax header with animated gradient background.
 *
 * Based on PRD specifications:
 * - Height: 200dp
 * - Background gradient: changes based on active tab (blue for Done, teal for Todo)
 * - Overlay gradient: transparent to BgPrimary 80%
 * - Back button: 44×44dp with blur background
 * - Content: bike name + total value with icon
 * - Transition animation: 300ms
 *
 * @param bikeName Name of the bike
 * @param totalValue Total km or hours
 * @param countingMethod Counting method (KM or HOURS)
 * @param activeTab Current active tab variant
 * @param onBackClick Callback when back button is clicked
 * @param modifier Modifier to be applied
 */
@Composable
fun ParallaxHeader(
    bikeName: String,
    totalValue: Float,
    countingMethod: CountingMethod,
    activeTab: TabVariant,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.HeaderHeight)
    ) {
        // Background gradient (animated based on active tab)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (activeTab == TabVariant.DONE) Gradients.headerDone
                    else Gradients.headerTodo
                )
        )

        // Overlay gradient (vertical fade to BgPrimary)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BgPrimary.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.Space2xl)
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(Dimens.BackButtonSize)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bike info
            Column {
                Text(
                    text = bikeName,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceSm))

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
                        text = formatValue(totalValue, countingMethod),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Format value with unit
 */
private fun formatValue(value: Float, method: CountingMethod): String {
    val unit = if (method == CountingMethod.KM) "km" else "h"
    return if (value % 1.0f == 0.0f) {
        "${formatNumber(value.toInt())} $unit"
    } else {
        "${formatNumberDecimal(value)} $unit"
    }
}
