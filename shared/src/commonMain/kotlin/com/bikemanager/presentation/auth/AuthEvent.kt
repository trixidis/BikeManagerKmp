package com.bikemanager.presentation.auth

/**
 * One-shot events sent from ViewModel to UI layer.
 * These are consumed once and don't persist in state.
 *
 * Events are delivered via Channel to guarantee one-time delivery.
 */
sealed interface AuthEvent {

    /**
     * Show an error message to the user (e.g., in a Snackbar).
     *
     * @param message The error message to display
     */
    data class ShowError(val message: String) : AuthEvent
}
