package com.bikemanager.presentation.auth

import com.bikemanager.domain.model.User

/**
 * UI state for the authentication screen.
 */
sealed class AuthUiState {
    /**
     * Initial state while checking auth status.
     */
    data object Checking : AuthUiState()

    /**
     * User is not authenticated, show sign-in UI.
     */
    data object NotAuthenticated : AuthUiState()

    /**
     * Sign-in is in progress.
     */
    data object Loading : AuthUiState()

    /**
     * User is authenticated.
     */
    data class Authenticated(val user: User) : AuthUiState()

    /**
     * An error occurred during authentication.
     */
    data class Error(val message: String) : AuthUiState()
}
