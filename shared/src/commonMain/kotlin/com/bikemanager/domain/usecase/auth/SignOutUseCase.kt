package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.repository.AuthRepository

/**
 * Use case for signing out the current user.
 */
class SignOutUseCase(private val repository: AuthRepository) {

    /**
     * Signs out the current user.
     */
    suspend operator fun invoke() {
        repository.signOut()
    }
}
