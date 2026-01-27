package com.bikemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikemanager.ui.theme.*

/**
 * Tab item data
 *
 * @param label Tab label text
 * @param count Number to display in badge
 * @param variant Tab color variant (Done or Todo)
 */
data class TabItem(
    val label: String,
    val count: Int,
    val variant: TabVariant
)

/**
 * Tab variant types
 */
enum class TabVariant {
    /** Blue gradient for "Done" items */
    DONE,
    /** Teal gradient for "Todo" items */
    TODO
}

/**
 * Premium tabs component with gradient backgrounds and count badges.
 *
 * Based on PRD specifications:
 * - Container: BgSecondary, border radius 16dp, padding 6dp
 * - Selected tab: gradient background + shadow
 * - Count badges with semi-transparent backgrounds
 * - Smooth transitions
 *
 * @param tabs List of tab items
 * @param selectedIndex Currently selected tab index
 * @param onTabSelected Callback when tab is selected
 * @param modifier Modifier to be applied
 */
@Composable
fun Tabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space2xl)
            .background(BgSecondary, RoundedCornerShape(Dimens.RadiusLg))
            .border(1.dp, BorderSubtle, RoundedCornerShape(Dimens.RadiusLg))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex

            val (background, shadowColor) = when {
                isSelected && tab.variant == TabVariant.DONE ->
                    Gradients.blue to AccentBlue
                isSelected && tab.variant == TabVariant.TODO ->
                    Gradients.teal to AccentTeal
                else -> Brush.linearGradient(
                    listOf(Color.Transparent, Color.Transparent)
                ) to Color.Transparent
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = if (isSelected) 4.dp else 0.dp,
                        shape = RoundedCornerShape(Dimens.RadiusMd),
                        ambientColor = shadowColor.copy(alpha = 0.3f),
                        spotColor = shadowColor.copy(alpha = 0.3f)
                    )
                    .background(background, RoundedCornerShape(Dimens.RadiusMd))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isSelected) Color.White else TextSecondary
                    )

                    // Count badge
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = Dimens.TabCountMinWidth)
                            .background(
                                Color.White.copy(
                                    alpha = if (isSelected) 0.25f else 0.2f
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
