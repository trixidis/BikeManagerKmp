package com.bikemanager.presentation.bikes

/**
 * One-shot events sent from ViewModel to UI layer.
 * These are consumed once and don't persist in state.
 *
 * Events are delivered via Channel to guarantee one-time delivery.
 * Success events use typed objects instead of raw strings so that
 * the UI layer can resolve localized string resources.
 */
sealed interface BikeEvent {

    /**
     * Show an error message to the user (e.g., in a Snackbar).
     *
     * @param message The error message to display
     */
    data class ShowError(val message: String) : BikeEvent

    /**
     * A bike was successfully added.
     */
    data object BikeAdded : BikeEvent

    /**
     * A bike was successfully deleted.
     */
    data object BikeDeleted : BikeEvent
}
