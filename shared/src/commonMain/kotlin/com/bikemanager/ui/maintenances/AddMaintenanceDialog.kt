package com.bikemanager.ui.maintenances

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.ui.Strings

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
        Strings.ADD_MAINTENANCE_DONE
    } else {
        Strings.ADD_MAINTENANCE_TODO
    }

    val valueHint = when (countingMethod) {
        CountingMethod.KM -> Strings.NB_KM_HINT
        CountingMethod.HOURS -> Strings.NB_HOURS_HINT
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
                    label = { Text(Strings.MAINTENANCE_TYPE_HINT) },
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
                Text(Strings.ADD)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.CANCEL)
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
        CountingMethod.KM -> Strings.NB_KM_HINT
        CountingMethod.HOURS -> Strings.NB_HOURS_HINT
    }

    val isValid = valueText.toFloatOrNull() != null && (valueText.toFloatOrNull() ?: -1f) >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.MARK_DONE_TITLE) },
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
                Text(Strings.CONFIRM_YES)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.CONFIRM_NO)
            }
        }
    )
}
