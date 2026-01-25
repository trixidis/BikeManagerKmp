package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance

/**
 * UI state for the maintenances screen.
 * Local-first: SQLite is the single source of truth.
 */
sealed class MaintenancesUiState {
    /**
     * Loading state while fetching maintenances from local database.
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
