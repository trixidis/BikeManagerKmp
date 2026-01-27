package com.bikemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.*

/**
 * Premium input field with focus and error states.
 *
 * Based on PRD specifications:
 * - Height: 56dp
 * - Padding: 16dp 18dp
 * - Border radius: 14dp
 * - Focus state: orange border + shadow
 * - Error state: red border + shadow
 * - Background: BgCard
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param label Label text (displayed above input, uppercase)
 * @param modifier Modifier to be applied
 * @param placeholder Placeholder text when empty
 * @param isError Whether the input is in error state
 * @param errorMessage Error message to display below input
 * @param keyboardType Type of keyboard to show
 */
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Input container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.InputHeight)
                .shadow(
                    elevation = if (isFocused && !isError) 3.dp else if (isError) 3.dp else 0.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = if (isError) ErrorRed.copy(alpha = 0.15f)
                    else AccentOrange.copy(alpha = 0.15f),
                    spotColor = if (isError) ErrorRed.copy(alpha = 0.15f)
                    else AccentOrange.copy(alpha = 0.15f)
                )
                .background(BgCard, RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> ErrorRed
                        isFocused -> AccentOrange
                        else -> BorderSubtle
                    },
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(AccentOrange),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Error message
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = ErrorRed,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
