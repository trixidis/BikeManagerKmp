package com.bikemanager.ui.maintenances

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bikemanager.domain.model.CountingMethod
import bikemanager.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.bikemanager.ui.components.BottomSheetDialog
import com.bikemanager.ui.components.ButtonPrimary
import com.bikemanager.ui.components.InputField
import com.bikemanager.ui.theme.Dimens

/**
 * Premium dialog for adding a maintenance.
 * Uses BottomSheetDialog with InputField and ButtonPrimary.
 */
@Composable
fun AddMaintenanceDialog(
    isDone: Boolean,
    countingMethod: CountingMethod,
    onDismiss: () -> Unit,
    onConfirm: (name: String, value: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }

    val title = if (isDone) {
        stringResource(Res.string.add_maintenance_done)
    } else {
        stringResource(Res.string.add_maintenance_todo)
    }

    val valueHint = when (countingMethod) {
        CountingMethod.KM -> stringResource(Res.string.nb_km_hint)
        CountingMethod.HOURS -> stringResource(Res.string.nb_hours_hint)
    }

    val isValid = name.isNotBlank() && (!isDone || valueText.toFloatOrNull() != null)

    BottomSheetDialog(
        title = title,
        onDismiss = onDismiss
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        InputField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(Res.string.maintenance_name)
        )

        if (isDone) {
            Spacer(modifier = Modifier.height(Dimens.SpaceXl))

            InputField(
                value = valueText,
                onValueChange = { valueText = it },
                label = valueHint,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space3xl))

        ButtonPrimary(
            text = stringResource(Res.string.confirm),
            onClick = {
                if (isValid) {
                    val value = if (isDone) valueText.toFloatOrNull() ?: 0f else -1f
                    onConfirm(name, value)
                }
            },
            enabled = isValid
        )
    }
}
