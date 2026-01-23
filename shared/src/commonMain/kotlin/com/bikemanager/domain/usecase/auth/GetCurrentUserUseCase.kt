package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the current authenticated user.
 */
class GetCurrentUserUseCase(private val repository: AuthRepository) {

    /**
     * Gets the current user synchronously.
     * @return The current user or null if not authenticated
     */
    operator fun invoke(): User? {
        return repository.getCurrentUser()
    }

    /**
     * Observes authentication state changes.
     * @return A Flow emitting the current user or null
     */
    fun observeAuthState(): Flow<User?> {
        return repository.observeAuthState()
    }

    /**
     * Checks if a user is currently signed in.
     */
    fun isSignedIn(): Boolean {
        return repository.isSignedIn()
    }
}
