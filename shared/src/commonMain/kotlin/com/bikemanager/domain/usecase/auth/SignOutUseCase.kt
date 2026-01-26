package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.repository.AuthRepository

/**
 * Use case for signing out the current user.
 */
class SignOutUseCase(private val repository: AuthRepository) {

    /**
     * Signs out the current user.
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return repository.signOut()
    }
}
