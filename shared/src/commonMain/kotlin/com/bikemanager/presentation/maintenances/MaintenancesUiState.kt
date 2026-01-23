package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance

/**
 * UI state for the maintenances screen.
 */
sealed class MaintenancesUiState {
    /**
     * Loading state while fetching maintenances.
     */
    data object Loading : MaintenancesUiState()

    /**
     * Success state with done and todo maintenance lists.
     */
    data class Success(
        val doneMaintenances: List<Maintenance>,
        val todoMaintenances: List<Maintenance>
    ) : MaintenancesUiState()

    /**
     * Error state with error message.
     */
    data class Error(val message: String) : MaintenancesUiState()
}
