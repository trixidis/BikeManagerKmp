package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.model.User
import com.bikemanager.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SignOutUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignOutUseCase

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = SignOutUseCase(repository)
    }

    @Test
    fun `invoke signs out the current user`() = runTest {
        // Setup: sign in a user first
        repository.setCurrentUser(
            User(
                uid = "test-uid",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null
            )
        )

        useCase()

        assertFalse(repository.isSignedIn())
        assertNull(repository.getCurrentUser())
    }

    @Test
    fun `invoke does nothing when no user is signed in`() = runTest {
        // Should not throw
        useCase()

        assertFalse(repository.isSignedIn())
    }
}
