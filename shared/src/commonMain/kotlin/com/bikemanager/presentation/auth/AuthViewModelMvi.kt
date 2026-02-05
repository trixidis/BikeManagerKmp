package com.bikemanager.presentation.auth

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.common.fold
import com.bikemanager.domain.usecase.auth.GetCurrentUserUseCase
import com.bikemanager.domain.usecase.auth.SignInUseCase
import com.bikemanager.domain.usecase.auth.SignInWithAppleUseCase
import com.bikemanager.domain.usecase.auth.SignOutUseCase
import com.bikemanager.presentation.base.MviViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing authentication state.
 * Follows MVI pattern with automatic state management from base class.
 *
 * Uses:
 * - StateFlow for persistent UI state (Checking, Loading, Authenticated, NotAuthenticated, Error)
 * - Channel for one-shot events (ShowError)
 * - Base MviViewModel for infrastructure (execute, emitEvent, updateState)
 *
 * CancellationException handling is guaranteed by base class.
 *
 * @param getCurrentUserUseCase Use case to get current authenticated user
 * @param signInUseCase Use case to sign in with Google
 * @param signInWithAppleUseCase Use case to sign in with Apple
 * @param signOutUseCase Use case to sign out
 */
class AuthViewModelMvi(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signInUseCase: SignInUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
    private val signOutUseCase: SignOutUseCase
) : MviViewModel<AuthUiState, AuthEvent>(
    initialState = AuthUiState.Checking
) {

    /**
     * UI State exposed for backward compatibility with existing UI.
     * Maps from base class state property.
     */
    val uiState: StateFlow<AuthUiState> = state

    init {
        checkAuthState()
    }

    // ========== Public API - Intent Handlers ==========

    /**
     * Check the current authentication state.
     *
     * Updates state based on whether a user is currently authenticated.
     * This is a synchronous operation (no network call).
     */
    fun checkAuthState() {
        updateState { AuthUiState.Checking }

        execute {
            val user = getCurrentUserUseCase()
            updateState {
                if (user != null) {
                    AuthUiState.Authenticated(user)
                } else {
                    AuthUiState.NotAuthenticated
                }
            }
            com.bikemanager.domain.common.Result.Success(Unit)
        }
    }

    /**
     * Sign in with Google using the provided ID token.
     *
     * On success:
     * - Updates state to Authenticated
     * - Navigates to main screen (handled by UI)
     *
     * On failure:
     * - Updates state to Error
     * - Emits ShowError event
     *
     * @param idToken The Google ID token
     */
    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            updateState { AuthUiState.Error(ErrorMessages.AUTH_INVALID_CREDENTIALS) }
            emitEvent(AuthEvent.ShowError(ErrorMessages.AUTH_INVALID_CREDENTIALS))
            return
        }

        updateState { AuthUiState.Loading }

        execute(
            onSuccess = { value ->
                val user = value as? com.bikemanager.domain.model.User
                if (user != null) {
                    updateState { AuthUiState.Authenticated(user) }
                }
            }
        ) {
            signInUseCase(idToken)
        }
    }

    /**
     * Sign in with Apple using the provided ID token.
     *
     * On success:
     * - Updates state to Authenticated
     * - Navigates to main screen (handled by UI)
     *
     * On failure:
     * - Updates state to Error
     * - Emits ShowError event
     *
     * @param idToken The Apple ID token
     * @param nonce Optional nonce for additional security
     */
    fun signInWithApple(idToken: String, nonce: String? = null) {
        if (idToken.isBlank()) {
            updateState { AuthUiState.Error(ErrorMessages.AUTH_INVALID_CREDENTIALS) }
            emitEvent(AuthEvent.ShowError(ErrorMessages.AUTH_INVALID_CREDENTIALS))
            return
        }

        updateState { AuthUiState.Loading }

        execute(
            onSuccess = { value ->
                val user = value as? com.bikemanager.domain.model.User
                if (user != null) {
                    updateState { AuthUiState.Authenticated(user) }
                }
            }
        ) {
            signInWithAppleUseCase(idToken, nonce)
        }
    }

    /**
     * Handle Apple Sign In flow using Firebase Auth SDK directly.
     *
     * This method orchestrates the entire Apple Sign In process:
     * 1. Creates native handler (iOS: Firebase iOS SDK, Android: Firebase Android SDK)
     * 2. Triggers the sign-in flow (Firebase gère le nonce et l'authentification)
     * 3. Handles the result (Success, Failure, Cancelled)
     * 4. Refreshes auth state since Firebase has already authenticated the user
     *
     * Note: Firebase Auth SDK gère directement l'authentification, donc on n'a pas besoin
     * d'appeler signInWithAppleUseCase. On rafraîchit juste l'état avec checkAuthState().
     *
     * @param onSuccess Callback when sign in completes successfully
     * @param onError Callback when sign in fails with error message
     */
    fun handleAppleSignIn(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        updateState { AuthUiState.Loading }

        execute {
            val handler = com.bikemanager.ui.auth.AppleSignInHandler()
            val result = handler.signIn()

            when (result) {
                is com.bikemanager.ui.auth.AppleSignInResult.Success -> {
                    Napier.d { "Apple Sign In handler success, Firebase has authenticated the user" }
                    // Firebase a déjà authentifié l'utilisateur en interne
                    // On rafraîchit juste l'état d'authentification
                    checkAuthState()
                    onSuccess()
                }
                is com.bikemanager.ui.auth.AppleSignInResult.Failure -> {
                    Napier.e { "Apple Sign In handler failure: ${result.error}" }
                    updateState { AuthUiState.Error(result.error) }
                    onError(result.error)
                }
                com.bikemanager.ui.auth.AppleSignInResult.Cancelled -> {
                    Napier.d { "Apple Sign In cancelled by user" }
                    // Ne pas afficher d'erreur, juste retourner à NotAuthenticated
                    updateState { AuthUiState.NotAuthenticated }
                }
            }

            com.bikemanager.domain.common.Result.Success(Unit)
        }
    }

    /**
     * Sign out the current user.
     *
     * On success or failure:
     * - Updates state to NotAuthenticated
     * - Returns to login screen (handled by UI)
     */
    fun signOut() {
        execute(
            onSuccess = {
                updateState { AuthUiState.NotAuthenticated }
            }
        ) {
            signOutUseCase().also {
                // Always transition to NotAuthenticated, even on error
                it.fold(
                    onSuccess = { },
                    onFailure = { error ->
                        Napier.e(error) { "Error signing out, but proceeding anyway" }
                    }
                )
                updateState { AuthUiState.NotAuthenticated }
            }
        }
    }

    /**
     * Clear error state and return to NotAuthenticated.
     *
     * Used when user dismisses an error message.
     */
    fun clearError() {
        if (_state.value is AuthUiState.Error) {
            updateState { AuthUiState.NotAuthenticated }
        }
    }

    /**
     * Set an error message.
     *
     * Used for client-side validation errors or external error handling.
     *
     * @param message The error message to display
     */
    fun setError(message: String) {
        updateState { AuthUiState.Error(message) }
        emitEvent(AuthEvent.ShowError(message))
    }

    // ========== Error Handling ==========

    /**
     * Convert errors to user-facing messages and update state.
     *
     * Called automatically by base class when operations fail.
     * Never receives CancellationException (handled by base class).
     *
     * For auth errors, we update the state to Error instead of just emitting an event,
     * as the error state needs to persist for the login screen.
     *
     * @param error The application-level error
     */
    override fun handleError(error: AppError) {
        Napier.e { "Auth error: ${error.message}" }
        val errorMessage = ErrorMessages.getMessage(error)
        updateState { AuthUiState.Error(errorMessage) }
        emitEvent(AuthEvent.ShowError(errorMessage))
    }
}
