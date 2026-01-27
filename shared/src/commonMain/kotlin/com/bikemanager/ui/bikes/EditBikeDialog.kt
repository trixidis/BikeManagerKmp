package com.bikemanager.ui.bikes

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.ui.Strings
import com.bikemanager.ui.components.BottomSheetDialog
import com.bikemanager.ui.components.ButtonPrimary
import com.bikemanager.ui.components.InputField
import com.bikemanager.ui.components.ToggleGroup
import com.bikemanager.ui.components.ToggleOption
import com.bikemanager.ui.theme.Dimens

/**
 * Premium dialog for editing a bike.
 * Uses BottomSheetDialog with InputField, ToggleGroup, and ButtonPrimary.
 */
@Composable
fun EditBikeDialog(
    bike: Bike,
    onDismiss: () -> Unit,
    onConfirm: (Bike) -> Unit
) {
    var bikeName by remember { mutableStateOf(bike.name) }
    var countingMethod by remember { mutableStateOf(bike.countingMethod) }

    BottomSheetDialog(
        title = Strings.EDIT_BIKE_TITLE,
        onDismiss = onDismiss
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        InputField(
            value = bikeName,
            onValueChange = { bikeName = it },
            label = Strings.MY_BIKES
        )

        Spacer(modifier = Modifier.height(Dimens.Space2xl))

        ToggleGroup(
            options = listOf(
                ToggleOption(
                    label = Strings.COUNTING_METHOD_KM,
                    value = "km",
                    icon = Icons.Default.Build
                ),
                ToggleOption(
                    label = Strings.COUNTING_METHOD_HOURS,
                    value = "hours",
                    icon = Icons.Default.Build
                )
            ),
            selectedValue = if (countingMethod == CountingMethod.KM) "km" else "hours",
            onValueChange = { value ->
                countingMethod = if (value == "km") CountingMethod.KM else CountingMethod.HOURS
            }
        )

        Spacer(modifier = Modifier.height(Dimens.Space3xl))

        ButtonPrimary(
            text = Strings.CONFIRM,
            onClick = {
                if (bikeName.isNotBlank()) {
                    onConfirm(bike.copy(name = bikeName, countingMethod = countingMethod))
                }
            },
            enabled = bikeName.isNotBlank()
        )
    }
}
