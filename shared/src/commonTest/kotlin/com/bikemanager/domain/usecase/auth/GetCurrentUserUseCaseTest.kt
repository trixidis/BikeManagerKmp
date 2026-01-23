package com.bikemanager.domain.usecase.auth

import app.cash.turbine.test
import com.bikemanager.domain.model.User
import com.bikemanager.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetCurrentUserUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: GetCurrentUserUseCase

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = GetCurrentUserUseCase(repository)
    }

    @Test
    fun `invoke returns null when no user is signed in`() {
        val user = useCase()
        assertNull(user)
    }

    @Test
    fun `invoke returns user when signed in`() {
        val testUser = User(
            uid = "test-uid",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        repository.setCurrentUser(testUser)

        val user = useCase()
        assertEquals(testUser, user)
    }

    @Test
    fun `isSignedIn returns false when no user is signed in`() {
        val isSignedIn = useCase.isSignedIn()
        assertFalse(isSignedIn)
    }

    @Test
    fun `isSignedIn returns true when user is signed in`() {
        repository.setCurrentUser(
            User(uid = "uid", email = null, displayName = null, photoUrl = null)
        )

        val isSignedIn = useCase.isSignedIn()
        assertTrue(isSignedIn)
    }

    @Test
    fun `observeAuthState emits null when not signed in`() = runTest {
        useCase.observeAuthState().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAuthState emits user when signed in`() = runTest {
        val testUser = User(
            uid = "test-uid",
            email = "test@example.com",
            displayName = null,
            photoUrl = null
        )

        useCase.observeAuthState().test {
            assertNull(awaitItem())

            repository.setCurrentUser(testUser)
            assertEquals(testUser, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAuthState emits null after sign out`() = runTest {
        val testUser = User(
            uid = "test-uid",
            email = "test@example.com",
            displayName = null,
            photoUrl = null
        )
        repository.setCurrentUser(testUser)

        useCase.observeAuthState().test {
            assertEquals(testUser, awaitItem())

            repository.setCurrentUser(null)
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
