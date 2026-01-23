package com.bikemanager.android.ui.maintenances

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bikemanager.android.R
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance

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
        stringResource(R.string.add_maintenance_done)
    } else {
        stringResource(R.string.add_maintenance_todo)
    }

    val valueHint = when (countingMethod) {
        CountingMethod.KM -> stringResource(R.string.nb_km_hint)
        CountingMethod.HOURS -> stringResource(R.string.nb_hours_hint)
    }

    val isValid = name.isNotBlank() && (!isDone || valueText.toFloatOrNull() != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.maintenance_type_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isDone) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        label = { Text(valueHint) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = if (isDone) valueText.toFloatOrNull() ?: 0f else 0f
                    onConfirm(name, value)
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun MarkDoneDialog(
    maintenance: Maintenance,
    countingMethod: CountingMethod,
    onDismiss: () -> Unit,
    onConfirm: (value: Float) -> Unit
) {
    var valueText by remember { mutableStateOf("") }

    val valueHint = when (countingMethod) {
        CountingMethod.KM -> stringResource(R.string.nb_km_hint)
        CountingMethod.HOURS -> stringResource(R.string.nb_hours_hint)
    }

    val isValid = valueText.toFloatOrNull() != null && (valueText.toFloatOrNull() ?: -1f) >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mark_done_title)) },
        text = {
            Column {
                Text(
                    text = maintenance.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text(valueHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = valueText.toFloatOrNull() ?: 0f
                    onConfirm(value)
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.no))
            }
        }
    )
}
