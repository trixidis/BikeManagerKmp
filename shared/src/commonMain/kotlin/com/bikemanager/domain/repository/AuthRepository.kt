package com.bikemanager.domain.repository

import com.bikemanager.domain.common.Result
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
     * @return Result containing the authenticated user or an error
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Signs in with Apple using the provided ID token.
     * @param idToken The Apple ID token
     * @param nonce Optional nonce for additional security
     * @return Result containing the authenticated user or an error
     */
    suspend fun signInWithApple(idToken: String, nonce: String? = null): Result<User>

    /**
     * Signs out the current user.
     * @return Result indicating success or failure
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Deletes all user data from Firebase Realtime Database (users/{uid}/).
     * @param uid The user ID whose data should be deleted
     * @return Result indicating success or failure
     */
    suspend fun deleteUserData(uid: String): Result<Unit>

    /**
     * Deletes the current Firebase Auth account.
     * @return Result indicating success or failure
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Checks if a user is currently signed in.
     */
    fun isSignedIn(): Boolean
}
