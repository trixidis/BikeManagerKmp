package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance

/**
 * One-shot events sent from ViewModel to UI layer.
 * These are consumed once and don't persist in state.
 *
 * Events are delivered via Channel to guarantee one-time delivery.
 */
sealed interface MaintenanceEvent {

    /**
     * Show an error message to the user (e.g., in a Snackbar).
     *
     * @param message The error message to display
     */
    data class ShowError(val message: String) : MaintenanceEvent

    /**
     * Show a success message to the user.
     *
     * @param message The success message to display
     */
    data class ShowSuccess(val message: String) : MaintenanceEvent

    /**
     * Show a snackbar with undo option after maintenance deletion.
     *
     * @param maintenance The deleted maintenance that can be restored
     */
    data class ShowUndoSnackbar(val maintenance: Maintenance) : MaintenanceEvent
}
