package com.bikemanager.presentation.bikes

import com.bikemanager.domain.model.Bike

/**
 * UI state for the bikes screen.
 */
sealed class BikesUiState {
    /**
     * Loading state while fetching bikes.
     */
    data object Loading : BikesUiState()

    /**
     * Success state with list of bikes.
     */
    data class Success(val bikes: List<Bike>) : BikesUiState()

    /**
     * Error state with error message.
     */
    data class Error(val message: String) : BikesUiState()

    /**
     * Empty state when no bikes exist.
     */
    data object Empty : BikesUiState()
}
