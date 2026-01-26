package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance

/**
 * Represents the UI state for the maintenances screen.
 * Sealed interface ensures exhaustive when expressions in UI layer.
 */
sealed interface MaintenanceState {

    /**
     * Initial loading state when data is being fetched.
     */
    data object Loading : MaintenanceState

    /**
     * Successfully loaded maintenances with data.
     *
     * @param doneMaintenances List of completed maintenances
     * @param todoMaintenances List of pending maintenances
     */
    data class Loaded(
        val doneMaintenances: List<Maintenance>,
        val todoMaintenances: List<Maintenance>
    ) : MaintenanceState

    /**
     * Error state with user-facing message.
     *
     * @param message The error message to display to the user
     */
    data class Error(val message: String) : MaintenanceState
}
