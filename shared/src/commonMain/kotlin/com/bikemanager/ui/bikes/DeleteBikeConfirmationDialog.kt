package com.bikemanager.ui.bikes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.bikemanager.domain.model.Bike
import com.bikemanager.ui.Strings

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
        title = { Text(Strings.DELETE_BIKE_TITLE) },
        text = {
            Text(
                text = Strings.DELETE_BIKE_MESSAGE,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = Strings.DELETE,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.CANCEL)
            }
        }
    )
}
