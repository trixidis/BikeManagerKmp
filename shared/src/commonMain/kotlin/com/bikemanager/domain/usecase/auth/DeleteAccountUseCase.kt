package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.repository.AuthRepository

/**
 * Use case for deleting a user account and all associated data.
 *
 * Deletion order:
 * 1. Delete all user data from Firebase Realtime Database (users/{uid}/)
 * 2. Delete the Firebase Auth account
 *
 * If any step fails, the operation stops and returns the error.
 */
class DeleteAccountUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Deletes the current user's data and account.
     *
     * @return Result<Unit> Success if deleted, Failure with AppError otherwise
     */
    suspend operator fun invoke(): Result<Unit> {
        val user = authRepository.getCurrentUser()
            ?: return Result.Failure(AppError.AuthError("User not authenticated"))

        // 1. Delete all user data from database
        val deleteDataResult = authRepository.deleteUserData(user.uid)
        if (deleteDataResult is Result.Failure) return deleteDataResult

        // 2. Delete the Firebase Auth account
        return authRepository.deleteAccount()
    }
}
