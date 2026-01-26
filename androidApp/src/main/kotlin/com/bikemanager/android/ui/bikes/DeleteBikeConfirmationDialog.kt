package com.bikemanager.android.ui.bikes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bikemanager.android.R
import com.bikemanager.domain.model.Bike

/**
 * Confirmation dialog for deleting a bike.
 * Warns the user that all associated maintenances will also be deleted.
 *
 * @param bike The bike to be deleted
 * @param onDismiss Callback when dialog is dismissed without confirmation
 * @param onConfirm Callback when user confirms deletion
 */
@Composable
fun DeleteBikeConfirmationDialog(
    bike: Bike,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_bike_title)) },
        text = {
            Text(
                text = stringResource(R.string.delete_bike_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
