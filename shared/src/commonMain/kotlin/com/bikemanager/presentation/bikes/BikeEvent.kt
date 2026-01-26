package com.bikemanager.presentation.bikes

/**
 * One-shot events sent from ViewModel to UI layer.
 * These are consumed once and don't persist in state.
 *
 * Events are delivered via Channel to guarantee one-time delivery.
 */
sealed interface BikeEvent {

    /**
     * Show an error message to the user (e.g., in a Snackbar).
     *
     * @param message The error message to display
     */
    data class ShowError(val message: String) : BikeEvent

    /**
     * Show a success message to the user.
     *
     * @param message The success message to display
     */
    data class ShowSuccess(val message: String) : BikeEvent
}
