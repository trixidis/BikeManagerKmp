package com.bikemanager.presentation.bikes

import com.bikemanager.domain.model.Bike

/**
 * Sync status indicator for background synchronization.
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

/**
 * UI state for the bikes screen.
 * Uses a local-first approach where local data is displayed immediately
 * while cloud sync happens in the background.
 */
sealed class BikesUiState {
    /**
     * Loading state while fetching bikes from local database.
     * This should be brief as local DB access is fast.
     */
    data object Loading : BikesUiState()

    /**
     * Success state with list of bikes.
     * @param bikes The list of bikes from local database
     * @param syncStatus The current cloud synchronization status
     */
    data class Success(
        val bikes: List<Bike>,
        val syncStatus: SyncStatus = SyncStatus.IDLE
    ) : BikesUiState()

    /**
     * Error state with error message.
     */
    data class Error(val message: String) : BikesUiState()

    /**
     * Empty state when no bikes exist.
     * @param syncStatus The current cloud synchronization status
     */
    data class Empty(val syncStatus: SyncStatus = SyncStatus.IDLE) : BikesUiState()
}
