package com.bikemanager.domain.repository

import com.bikemanager.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    /**
     * Gets the current authenticated user, or null if not authenticated.
     */
    fun getCurrentUser(): User?

    /**
     * Observes authentication state changes.
     */
    fun observeAuthState(): Flow<User?>

    /**
     * Signs in with Google using the provided ID token.
     * @param idToken The Google ID token
     * @return The authenticated user
     */
    suspend fun signInWithGoogle(idToken: String): User

    /**
     * Signs out the current user.
     */
    suspend fun signOut()

    /**
     * Checks if a user is currently signed in.
     */
    fun isSignedIn(): Boolean
}
