package com.bikemanager.presentation.bikes

import com.bikemanager.domain.model.Bike

/**
 * UI state for the bikes screen.
 * Local-first: SQLite is the single source of truth.
 */
sealed class BikesUiState {
    /**
     * Loading state while fetching bikes from local database.
     */
    data object Loading : BikesUiState()

    /**
     * Success state with list of bikes from local database.
     */
    data class Success(val bikes: List<Bike>) : BikesUiState()

    /**
     * Error state with error message.
     */
    data class Error(val message: String) : BikesUiState()

    /**
     * Empty state when no bikes exist locally.
     */
    data class Empty(val placeholder: Unit = Unit) : BikesUiState()
}
