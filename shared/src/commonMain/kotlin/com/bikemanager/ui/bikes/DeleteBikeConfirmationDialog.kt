package com.bikemanager.ui.bikes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.bikemanager.domain.model.Bike
import bikemanager.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(Res.string.delete_bike_title),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.delete_bike_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(Res.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.cancel),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
