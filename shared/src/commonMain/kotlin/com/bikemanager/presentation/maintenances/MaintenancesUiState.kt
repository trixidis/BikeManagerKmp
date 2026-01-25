package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.presentation.bikes.SyncStatus

/**
 * UI state for the maintenances screen.
 * Uses local-first approach where data is loaded from local database.
 */
sealed class MaintenancesUiState {
    /**
     * Loading state while fetching maintenances from local database.
     */
    data object Loading : MaintenancesUiState()

    /**
     * Success state with done and todo maintenance lists.
     * @param doneMaintenances List of completed maintenances
     * @param todoMaintenances List of pending maintenances
     * @param syncStatus Current cloud synchronization status
     */
    data class Success(
        val doneMaintenances: List<Maintenance>,
        val todoMaintenances: List<Maintenance>,
        val syncStatus: SyncStatus = SyncStatus.IDLE
    ) : MaintenancesUiState()

    /**
     * Error state with error message.
     */
    data class Error(val message: String) : MaintenancesUiState()
}
