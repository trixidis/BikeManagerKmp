package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository

/**
 * Use case for signing in with Google.
 */
class SignInUseCase(private val repository: AuthRepository) {

    /**
     * Signs in with Google using the provided ID token.
     * @param idToken The Google ID token obtained from Google Sign-In
     * @return The authenticated user
     * @throws Exception if sign-in fails
     */
    suspend operator fun invoke(idToken: String): User {
        require(idToken.isNotBlank()) { "ID token cannot be empty" }
        return repository.signInWithGoogle(idToken)
    }
}
