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
import bikemanager.shared.generated.resources.*
import com.bikemanager.ui.Constants
import org.jetbrains.compose.resources.stringResource
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
        title = stringResource(Res.string.edit_bike_title),
        onDismiss = onDismiss
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        InputField(
            value = bikeName,
            onValueChange = { bikeName = it },
            label = stringResource(Res.string.my_bikes)
        )

        Spacer(modifier = Modifier.height(Dimens.Space2xl))

        ToggleGroup(
            options = listOf(
                ToggleOption(
                    label = stringResource(Res.string.counting_method_km),
                    value = Constants.COUNTING_METHOD_VALUE_KM,
                    icon = Icons.Default.Build
                ),
                ToggleOption(
                    label = stringResource(Res.string.counting_method_hours),
                    value = Constants.COUNTING_METHOD_VALUE_HOURS,
                    icon = Icons.Default.Build
                )
            ),
            selectedValue = if (countingMethod == CountingMethod.KM) Constants.COUNTING_METHOD_VALUE_KM else Constants.COUNTING_METHOD_VALUE_HOURS,
            onValueChange = { value ->
                countingMethod = if (value == Constants.COUNTING_METHOD_VALUE_KM) CountingMethod.KM else CountingMethod.HOURS
            }
        )

        Spacer(modifier = Modifier.height(Dimens.Space3xl))

        ButtonPrimary(
            text = stringResource(Res.string.confirm),
            onClick = {
                if (bikeName.isNotBlank()) {
                    onConfirm(bike.copy(name = bikeName, countingMethod = countingMethod))
                }
            },
            enabled = bikeName.isNotBlank()
        )
    }
}
