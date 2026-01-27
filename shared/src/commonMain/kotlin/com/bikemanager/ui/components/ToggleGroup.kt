package com.bikemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.*

/**
 * Toggle option data class
 *
 * @param label Display text for the option
 * @param value Value associated with this option
 * @param icon Icon to display alongside the label
 */
data class ToggleOption(
    val label: String,
    val value: String,
    val icon: ImageVector
)

/**
 * Premium toggle group for selecting between options (e.g., Km/Hours).
 *
 * Based on PRD specifications:
 * - Container: background BgCard, border radius 12dp, border subtle
 * - Padding: 4dp
 * - Options: equal width, padding 12dp 16dp
 * - Selected state: orange gradient + shadow
 * - Icon + text layout with 6dp gap
 *
 * @param options List of toggle options
 * @param selectedValue Currently selected value
 * @param onValueChange Callback when selection changes
 * @param modifier Modifier to be applied
 */
@Composable
fun ToggleGroup(
    options: List<ToggleOption>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.value == selectedValue

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = if (isSelected) 2.dp else 0.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = AccentOrange.copy(alpha = 0.3f),
                        spotColor = AccentOrange.copy(alpha = 0.3f)
                    )
                    .background(
                        brush = if (isSelected) Gradients.orange
                        else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onValueChange(option.value) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) Color.White else TextSecondary
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}
