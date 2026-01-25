package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.common.flatMap
import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository

/**
 * Use case for signing in with Google.
 */
class SignInUseCase(private val repository: AuthRepository) {

    /**
     * Signs in with Google using the provided ID token.
     * @param idToken The Google ID token obtained from Google Sign-In
     * @return Result containing the authenticated user or an error
     */
    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank()) {
            return Result.Failure(AppError.ValidationError(
                errorMessage = "ID token cannot be empty"
            ))
        }
        return repository.signInWithGoogle(idToken)
    }
}
