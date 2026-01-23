package com.bikemanager.android.ui.bikes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bikemanager.android.R
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod

@Composable
fun AddBikeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var bikeName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_bike_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.add_bike_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = bikeName,
                    onValueChange = { bikeName = it },
                    label = { Text(stringResource(R.string.my_bikes)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(bikeName) },
                enabled = bikeName.isNotBlank()
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
fun EditBikeDialog(
    bike: Bike,
    onDismiss: () -> Unit,
    onConfirm: (Bike) -> Unit
) {
    var bikeName by remember { mutableStateOf(bike.name) }
    var countingMethod by remember { mutableStateOf(bike.countingMethod) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_bike_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = bikeName,
                    onValueChange = { bikeName = it },
                    label = { Text(stringResource(R.string.my_bikes)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.count_by),
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(Modifier.selectableGroup()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = countingMethod == CountingMethod.KM,
                                onClick = { countingMethod = CountingMethod.KM },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = countingMethod == CountingMethod.KM,
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.km),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = countingMethod == CountingMethod.HOURS,
                                onClick = { countingMethod = CountingMethod.HOURS },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = countingMethod == CountingMethod.HOURS,
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.hours),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(bike.copy(name = bikeName, countingMethod = countingMethod))
                },
                enabled = bikeName.isNotBlank()
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
