package com.bikemanager.ui.maintenances

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import bikemanager.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.bikemanager.ui.components.BottomSheetDialog
import com.bikemanager.ui.components.ButtonPrimary
import com.bikemanager.ui.components.InputField
import com.bikemanager.ui.theme.Dimens
import com.bikemanager.ui.theme.TextSecondary

/**
 * Premium dialog for marking a maintenance as done.
 * Uses BottomSheetDialog with InputField and ButtonPrimary.
 */
@Composable
fun MarkDoneDialog(
    maintenance: Maintenance,
    countingMethod: CountingMethod,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var valueText by remember { mutableStateOf("") }

    val valueHint = when (countingMethod) {
        CountingMethod.KM -> stringResource(Res.string.nb_km_hint)
        CountingMethod.HOURS -> stringResource(Res.string.nb_hours_hint)
    }

    val isValid = valueText.toFloatOrNull() != null

    BottomSheetDialog(
        title = stringResource(Res.string.mark_done_title),
        onDismiss = onDismiss
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        Text(
            text = maintenance.name,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(Dimens.Space2xl))

        InputField(
            value = valueText,
            onValueChange = { valueText = it },
            label = valueHint,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(Dimens.Space3xl))

        ButtonPrimary(
            text = stringResource(Res.string.confirm),
            onClick = {
                val value = valueText.toFloatOrNull()
                if (value != null) {
                    onConfirm(value)
                }
            },
            enabled = isValid
        )
    }
}
