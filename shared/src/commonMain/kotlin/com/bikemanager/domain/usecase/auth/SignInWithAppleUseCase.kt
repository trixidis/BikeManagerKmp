package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository

/**
 * Use case for signing in with Apple.
 */
class SignInWithAppleUseCase(private val repository: AuthRepository) {

    /**
     * Signs in with Apple using the provided ID token.
     * @param idToken The Apple ID token obtained from Apple Sign-In
     * @param nonce Optional nonce for additional security
     * @return Result containing the authenticated user or an error
     */
    suspend operator fun invoke(idToken: String, nonce: String? = null): Result<User> {
        if (idToken.isBlank()) {
            return Result.Failure(AppError.ValidationError(
                errorMessage = "ID token cannot be empty"
            ))
        }
        return repository.signInWithApple(idToken, nonce)
    }
}
