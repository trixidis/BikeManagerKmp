package com.bikemanager.presentation.auth

import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.common.fold
import com.bikemanager.domain.usecase.auth.GetCurrentUserUseCase
import com.bikemanager.domain.usecase.auth.SignInUseCase
import com.bikemanager.domain.usecase.auth.SignOutUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state.
 */
class AuthViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Checking)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * Checks the current authentication state.
     */
    fun checkAuthState() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Checking
            try {
                val user = getCurrentUserUseCase()
                _uiState.value = if (user != null) {
                    AuthUiState.Authenticated(user)
                } else {
                    AuthUiState.NotAuthenticated
                }
            } catch (e: Exception) {
                Napier.e(e) { "Error checking auth state" }
                _uiState.value = AuthUiState.NotAuthenticated
            }
        }
    }

    /**
     * Signs in with Google using the provided ID token.
     * @param idToken The Google ID token
     */
    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.value = AuthUiState.Error("Invalid credentials")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = signInUseCase(idToken)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                },
                onFailure = { error ->
                    Napier.e(error) { "Error signing in" }
                    val message = if (error is com.bikemanager.domain.common.AppError) {
                        ErrorMessages.getMessage(error)
                    } else {
                        error.message ?: "Une erreur est survenue"
                    }
                    _uiState.value = AuthUiState.Error(message)
                }
            )
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            val result = signOutUseCase()
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.NotAuthenticated
                },
                onFailure = { error ->
                    Napier.e(error) { "Error signing out" }
                    // Optionally set error state, but for sign out we might just silently fail
                    _uiState.value = AuthUiState.NotAuthenticated
                }
            )
        }
    }

    /**
     * Resets the error state to not authenticated.
     */
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.NotAuthenticated
        }
    }

    /**
     * Sets an error message.
     */
    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }
}
