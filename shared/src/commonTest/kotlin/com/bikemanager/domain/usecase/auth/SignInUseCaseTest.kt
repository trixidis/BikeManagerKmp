package com.bikemanager.domain.usecase.auth

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SignInUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignInUseCase

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = SignInUseCase(repository)
    }

    @Test
    fun `invoke signs in user and returns user`() = runTest {
        val result = useCase("test-id-token")

        assertTrue(result is Result.Success)
        val user = result.value
        assertNotNull(user)
        assertTrue(user.uid.contains("test-id-token"))
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
    }

    @Test
    fun `invoke returns failure when id token is empty`() = runTest {
        val result = useCase("")

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke returns failure when id token is blank`() = runTest {
        val result = useCase("   ")

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke returns failure when sign in fails`() = runTest {
        repository.setSignInFails(true, AppError.AuthError("Auth failed"))

        val result = useCase("valid-token")

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke updates repository state`() = runTest {
        val result = useCase("test-token")

        assertTrue(result is Result.Success)
        assertTrue(repository.isSignedIn())
        assertNotNull(repository.getCurrentUser())
    }
}
